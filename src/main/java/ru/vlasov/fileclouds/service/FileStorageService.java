package ru.vlasov.fileclouds.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.vlasov.fileclouds.config.security.AppUserDetails;
import ru.vlasov.fileclouds.web.dto.StorageDto;
import ru.vlasov.fileclouds.web.dto.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileStorageService {

    @Value("${minio.root_bucket_name}")
    private String rootBucketName;

    private final MinioClient minioClient;

    @Autowired
    public FileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void createAppRootBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs
                    .builder()
                    .bucket(rootBucketName)
                    .build())
            ) {
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(rootBucketName)
                                .build());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 NoSuchAlgorithmException | IOException | ServerException | XmlParserException |
                 InvalidKeyException e) {
            throw new RuntimeException("Storage service doesn't answer");
        }
    }

    public void createFolder(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        path = checkAndMakeStringEndingWithSlash(path);
        String fullPath = getRootFolder() + path;

        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(rootBucketName)
                .object(fullPath)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    @NotNull
    private static String checkAndMakeStringEndingWithSlash(String string) {
        if (!string.endsWith("/")) {
            string = string + "/";
        }
        return string;
    }

    public void createRootUserDirectory(String rootDirectory) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        rootDirectory = checkAndMakeStringEndingWithSlash(rootDirectory);

        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(rootBucketName)
                .object(rootDirectory)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    public List<StorageDto> getFilesAndDirectories(String directory) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String fullPath = getRootFolder() + directory;
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(rootBucketName)
                        .prefix(fullPath)
                        .build());

        List<StorageDto> storageDtoList = new ArrayList<>();

        for (Result<Item> item : results) {
            StorageDto storageDto = Util.convertItemToStorageDto(item.get());
            if (storageDto != null) {
                storageDtoList.add(storageDto);
            }
        }

        return storageDtoList;
    }

    public void delete(String deleteName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (!deleteName.endsWith("/")) {
            String fullPath = getRootFolder() + deleteName;
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucketName)
                            .object(fullPath)
                            .build()
            );
        } else {
            List<String> fullPathsName = getAllfullPathNameObjectsWithParent(deleteName);

            for (String fullPath:fullPathsName) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(rootBucketName)
                                .object(fullPath)
                                .build()
                );
            }
        }
    }

    public void uploadFile(String bucket, String sourcePath) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        String[] paths = sourcePath.split(Pattern.quote(File.separator));
        String fileName = paths[paths.length - 1];
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .filename(sourcePath)
                        .build());
    }

    public void rename(String oldName, String newName, String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        if (!oldName.endsWith("/")) {

            newName = checkAndMakeNameWithPostfix(newName, oldName);
            minioClient.copyObject(CopyObjectArgs
                    .builder()
                            .bucket(rootBucketName)
                            .object(getRootFolder() + path + newName)
                            .source(CopySource
                                    .builder()
                                    .bucket(rootBucketName)
                                    .object(getRootFolder() + path + oldName)
                                    .build())
                    .build());

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucketName)
                            .object(getRootFolder() + path + oldName)
                            .build()
            );

        } else {
            newName = checkAndMakeStringEndingWithSlash(newName);
            List<String> fullPathsName = getAllfullPathNameObjectsWithParent(path + oldName);
            for (String fullPath:fullPathsName) {
                String newFullPath = fullPath.replace(path + oldName, path + newName);

                if (newFullPath.endsWith("/")) {
                    minioClient.putObject(PutObjectArgs
                            .builder()
                            .bucket(rootBucketName)
                            .object(newFullPath)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
                } else {
                    minioClient.copyObject(CopyObjectArgs
                            .builder()
                            .bucket(rootBucketName)
                            .object(newFullPath)
                            .source(CopySource
                                    .builder()
                                    .bucket(rootBucketName)
                                    .object(fullPath)
                                    .build())
                            .build());
                }
                
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(rootBucketName)
                                .object(fullPath)
                                .build()
                );
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

    private List<String> getAllfullPathNameObjectsWithParent(String folderName) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        Queue<String> folders = new PriorityQueue<>();
        folders.add(getRootFolder() + folderName);

        List<String> objectsName = new ArrayList<>();
        objectsName.add(getRootFolder() + folderName);

        while (!folders.isEmpty()) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs
                            .builder()
                            .bucket(rootBucketName)
                            .prefix(folders.remove())
                            .build());

            for (Result<Item> item : results) {
                if (item.get().isDir()) {
                    folders.add(item.get().objectName());
                }
                objectsName.add(item.get().objectName());
            }
        }
        return objectsName;
    }
}
