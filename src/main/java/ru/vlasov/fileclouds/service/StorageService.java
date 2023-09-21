package ru.vlasov.fileclouds.service;

import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vlasov.fileclouds.config.security.AppUserDetails;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.repository.MinioRepository;
import ru.vlasov.fileclouds.web.dto.StorageDto;
import ru.vlasov.fileclouds.web.dto.Util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class StorageService {

    private final MinioRepository minioRepository;

    @Autowired
    public StorageService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public void uploadFile(String path, MultipartFile multipartFile) throws BrokenFileException, StorageErrorException {
        String fullPath = getRootFolder() + path;
        minioRepository.uploadFile(fullPath, multipartFile);
    }

    public void createDirectory(String path) throws StorageErrorException {
        String fullPath = getRootFolder() + path;
        minioRepository.createDirectory(fullPath);
    }

    public void createRootUserDirectory(String rootDirectory) throws StorageErrorException {
        minioRepository.createDirectory(rootDirectory);
    }

    public List<StorageDto> getFilesAndDirectories(String directory) throws StorageErrorException {
        String fullPath = getRootFolder() + directory;
        Iterable<Result<Item>> results = minioRepository.getFilesAndDirectories(fullPath);

        List<StorageDto> storageDtoList = new ArrayList<>();
        for (Result<Item> item : results) {
            StorageDto storageDto;
            try {
                storageDto = Util.convertItemToStorageDto(item.get());
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new StorageErrorException("Storage server error");
            }
            if (storageDto != null) {
                storageDtoList.add(storageDto);
            }
        }
        return sortIsDirThenLastModified(storageDtoList);
    }

    @NotNull
    private static List<StorageDto> sortIsDirThenLastModified(List<StorageDto> storageDtoList) {
        return storageDtoList.stream().sorted(Comparator.comparing(StorageDto::isDir).reversed().thenComparing(StorageDto::getLastModified)).collect(Collectors.toList());
    }

    public void delete(String path) throws StorageErrorException {
        String fullPath = getRootFolder() + path;
        if (!path.endsWith("/")) {
            minioRepository.delete(fullPath);
        } else {
            List<String> fullPathNames = minioRepository.getAllfullPathNameObjectsWithParent(fullPath);
            for (String element : fullPathNames) {
                minioRepository.delete(element);
            }
        }
    }

    public void rename(String oldName, String newName, String path) throws StorageErrorException {

        if (!oldName.endsWith("/")) {

            newName = checkAndMakeNameWithPostfix(newName, oldName);
            minioRepository.copy(getRootFolder() + path + newName, getRootFolder() + path + oldName);
            minioRepository.delete(getRootFolder() + path + oldName);

        } else {
            List<String> fullPathsName = minioRepository.getAllfullPathNameObjectsWithParent(path + oldName);
            for (String fullPath : fullPathsName) {
                String newFullPath = fullPath.replace(getRootFolder() + path + oldName, getRootFolder() + path + newName);

                if (newFullPath.endsWith("/")) {
                    minioRepository.createDirectory(newFullPath);
                } else {
                    minioRepository.copy(newFullPath, fullPath);
                }
                minioRepository.delete(fullPath);
            }
        }
    }

    private String getRootFolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails appUserDetails = (AppUserDetails) authentication.getPrincipal();
        return "user-" + appUserDetails.getAppUser().getId() + "-files/";
    }

    private String checkAndMakeNameWithPostfix(String newName, String oldName) {
        String[] oldNameSplit = oldName.split("\\.");
        String oldPostfix = oldNameSplit[oldNameSplit.length - 1];

        String[] newNameSplit = newName.split("\\.");
        String newPostfix = newNameSplit[newNameSplit.length - 1];

        if (!newPostfix.equals(oldPostfix)) {
            newName = newName + "." + oldPostfix;
        }

        return newName;
    }

    public void uploadDirectory(String path, MultipartFile[] multipartFiles) throws BrokenFileException, StorageErrorException {
        Set<String> paths = new HashSet<>();
        for (MultipartFile file : multipartFiles) {
            if (Objects.requireNonNull(file.getOriginalFilename()).contains("/")) {
                String[] elements = file.getOriginalFilename().split("/");
                log.info("split element -> {}", Arrays.toString(elements));
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < elements.length - 1; i++) {
                    stringBuilder.append(elements[i]).append("/");
                    paths.add(stringBuilder.toString());
                }
            }
            minioRepository.uploadFile(getRootFolder() + path, file);
        }

        if (!paths.isEmpty()) {
            for (String element : paths) {
                minioRepository.createDirectory(getRootFolder() + element);
            }
        }
    }

    public InputStream download(String fullPath) throws StorageErrorException {
        return minioRepository.downloadFile(getRootFolder() + fullPath);
    }

    public void downloadZip(ZipOutputStream zipOut, String fullPath) throws StorageErrorException, IOException {
        String parentPath = getRootFolder() + fullPath;
        List<String> listFileNamesWithParent = minioRepository.getAllfullPathNameObjectsWithParent(parentPath);
        for (String fileName : listFileNamesWithParent) {
            log.info("fileName in list -> {}", fileName);
        }
        for (String fileName : listFileNamesWithParent) {
            if (!fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName.substring(parentPath.length())));
                try (InputStream fis = minioRepository.downloadFile(fileName)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                }
                zipOut.closeEntry();
            }
        }
    }

    public List<StorageDto> getFilesAndDirectoriesForQuery(String query) throws StorageErrorException {
        List<StorageDto> storageDtoList = new ArrayList<>();
        List<Item> itemList = minioRepository.getAllObjectListFromDir(getRootFolder());
        for (Item item:itemList) {
            StorageDto storageDto = Util.convertItemToStorageDto(item);
            assert storageDto != null;
            log.info("equals -> {} : {}", storageDto.getName().toLowerCase(), query.toLowerCase());
            if (storageDto.getName().toLowerCase().contains(query.toLowerCase())) {
                storageDtoList.add(storageDto);
            }
        }

        return sortIsDirThenLastModified(storageDtoList);
    }
}
