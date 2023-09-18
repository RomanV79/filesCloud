package ru.vlasov.fileclouds.service;

import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
public class StorageService {

    @Value("${minio.root_bucket_name}")
    private String rootBucketName;

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
            StorageDto storageDto = null;
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
        return storageDtoList;
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
        for (MultipartFile file:multipartFiles) {
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
        log.info("set path element -> {}", paths.toString());
        if (!paths.isEmpty()) {
            for (String element:paths) {
                minioRepository.createDirectory( getRootFolder() + element);
            }
        }
    }
}
