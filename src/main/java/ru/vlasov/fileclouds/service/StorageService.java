package ru.vlasov.fileclouds.service;

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
import ru.vlasov.fileclouds.customException.EmptyFolderException;
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.repository.MinioRepository;
import ru.vlasov.fileclouds.web.dto.StorageDto;
import ru.vlasov.fileclouds.web.dto.Util;

import java.io.IOException;
import java.io.InputStream;
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

    public List<StorageDto> getFilesAndDirsForCurrentPath(String directory) throws StorageErrorException, EmptyFolderException {
        String fullPath = getRootFolder() + directory;
        List<Item> results = minioRepository.getAllObjectListFromDirIncludeInternal(fullPath, false);
        if (isDirEmpty(results)) throw new EmptyFolderException("Empty");

        List<StorageDto> storageDtoList = new ArrayList<>();
        for (Item item : results) {
            StorageDto storageDto;
            storageDto = Util.convertItemToStorageDto(item);
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
        log.info("fullPath -> {}", fullPath);
        if (!isDir(path)) {
            minioRepository.delete(fullPath);
        } else {

            List<Item> itemList = minioRepository.getAllObjectListFromDirIncludeInternal(fullPath, true);
            if (itemList != null && !itemList.isEmpty()) {
                for (Item item:itemList) {
                    StorageDto storageDto = Util.convertItemToStorageDto(item);
                    if (storageDto != null) {
                        String elementPath = getRootFolder() + storageDto.getParentDirPath() + storageDto.getName();
                        log.info("elementPath -> {}", elementPath);
                        minioRepository.delete(getRootFolder() + storageDto.getParentDirPath() + storageDto.getName());
                    }
                }
            }
            minioRepository.delete(fullPath);
        }
    }

    public void rename(String oldName, String newName, String path) throws StorageErrorException {
        if (!isDir(oldName)) {
            newName = checkAndMakeNameWithPostfix(newName, oldName);
            minioRepository.copy(getRootFolder() + path + newName, getRootFolder() + path + oldName);
            minioRepository.delete(getRootFolder() + path + oldName);
        } else {
            newName = checkAndMakeNameWithSlash(newName);
            List<Item> itemList = minioRepository.getAllObjectListFromDirIncludeInternal(getRootFolder() + path + oldName, true);

            String destPath = getRootFolder() + path + newName;
            String sourcePath = getRootFolder() + path + oldName;
            minioRepository.createDirectory(destPath);
            minioRepository.delete(sourcePath);
            if (!isDirEmpty(itemList)) {
                for (Item item : itemList) {
                    StorageDto storageDto = Util.convertItemToStorageDto(item);
                    if (storageDto != null) {
                        String elementSourcePath = getRootFolder() + storageDto.getParentDirPath() + storageDto.getName();
                        String elementDestPath = elementSourcePath.replace(path + oldName, path + newName);
                        if (storageDto.isDir()) {
                            minioRepository.createDirectory(elementDestPath);
                        } else {
                            minioRepository.copy(elementDestPath, elementSourcePath);
                        }
                        minioRepository.delete(elementSourcePath);
                    }
                }
            }
        }
    }

    private static boolean isDirEmpty(List<Item> itemList) {
        return itemList == null;
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

    private String checkAndMakeNameWithSlash(String name) {
        return name.endsWith("/") ? name : name + "/";
    }

    public void uploadDirectory(String path, MultipartFile[] multipartFiles) throws BrokenFileException, StorageErrorException {
        Set<String> paths = new HashSet<>();
        for (MultipartFile file : multipartFiles) {
            if (Objects.requireNonNull(file.getOriginalFilename()).contains("/")) {
                String[] elements = file.getOriginalFilename().split("/");
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

    public void downloadZip(ZipOutputStream zipOut, String path) throws StorageErrorException, IOException {
        List<Item> itemList = minioRepository.getAllObjectListFromDirIncludeInternal(getRootFolder() + path, true);
        for (Item item:itemList) {
            StorageDto storageDto = Util.convertItemToStorageDto(item);
            assert storageDto != null;
            if (!storageDto.isDir()) {
                String currentName = (storageDto.getParentDirPath() + storageDto.getName()).substring(path.length());
                log.info("currentName -> {}", currentName);
                zipOut.putNextEntry(new ZipEntry(currentName));
                try (InputStream fis = minioRepository.downloadFile(getRootFolder() + storageDto.getParentDirPath() + storageDto.getName())) {
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

    public List<StorageDto> getFilesAndDirectoriesForQuery(String query) throws StorageErrorException, EmptyFolderException {
        List<StorageDto> storageDtoList = new ArrayList<>();
        List<Item> itemList = minioRepository.getAllObjectListFromDirIncludeInternal(getRootFolder(), true);
        if (isDirEmpty(itemList)) throw new EmptyFolderException("Empty");

        for (Item item : itemList) {
            StorageDto storageDto = Util.convertItemToStorageDto(item);
            assert storageDto != null;
            if (storageDto.getName().toLowerCase().contains(query.toLowerCase())) {
                storageDtoList.add(storageDto);
            }
        }
        if (storageDtoList.isEmpty()) throw new EmptyFolderException("Empty");

        return sortIsDirThenLastModified(storageDtoList);
    }

    private static boolean isDir(String name) {
        return name.endsWith("/");
    }
}
