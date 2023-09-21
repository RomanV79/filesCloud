package ru.vlasov.fileclouds.repository;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.StorageErrorException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Repository
public class MinioRepository {

    @Value("${minio.root_bucket_name}")
    private String rootBucketName;

    private final MinioClient minioClient;

    @Autowired
    public MinioRepository(MinioClient minioClient) {
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

    public void uploadFile(String fullPath, @NotNull MultipartFile multipartFile) throws StorageErrorException, BrokenFileException {

        InputStream inputStream;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            throw new BrokenFileException("The file is corrupted and cannot be downloaded");
        }

        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(rootBucketName)
                    .object(fullPath + multipartFile.getOriginalFilename())
                    .stream(inputStream, -1, 10485760)
                    .contentType(multipartFile.getContentType())
                    .build());

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageErrorException("Storage server error");
        }
    }

    public void createDirectory(String fullPath) throws StorageErrorException {

        fullPath = checkAndMakeStringEndingWithSlash(fullPath);

        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(rootBucketName)
                    .object(fullPath)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageErrorException("Storage server error");
        }
    }

    public Iterable<Result<Item>> getFilesAndDirectories(String fullPath) {
        fullPath = checkAndMakeStringEndingWithSlash(fullPath);

        return minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(rootBucketName)
                        .prefix(fullPath)
                        .build());
    }

    public void delete(String fullPath) throws StorageErrorException {

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucketName)
                            .object(fullPath)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageErrorException("Storage server error");
        }
    }

    public void copy(String destPath, String sourcePath) throws StorageErrorException {
        try {
            minioClient.copyObject(CopyObjectArgs
                    .builder()
                    .bucket(rootBucketName)
                    .object(destPath)
                    .source(CopySource
                            .builder()
                            .bucket(rootBucketName)
                            .object(sourcePath)
                            .build())
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageErrorException("Storage server error");
        }
    }

    public List<String> getAllfullPathNameObjectsWithParent(String fullPath) throws StorageErrorException {
        Queue<String> folders = new PriorityQueue<>();
        folders.add(fullPath);

        List<String> objectsName = new ArrayList<>();
        objectsName.add(fullPath);

        while (!folders.isEmpty()) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs
                            .builder()
                            .bucket(rootBucketName)
                            .prefix(folders.remove())
                            .build());

            try {
                for (Result<Item> item : results) {
                    if (item.get().isDir()) {
                        folders.add(item.get().objectName());
                    }
                    objectsName.add(item.get().objectName());
                }
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new StorageErrorException("Storage server error");
            }
        }
        return objectsName;
    }

    public List<Item> getAllObjectListFromDirIncludeInternal(String rootDir, boolean include) throws StorageErrorException {
        Queue<Item> directories = new LinkedList<>();
        List<Item> allItems = new ArrayList<>();

        do {
            if (!directories.isEmpty()) {
                rootDir = directories.remove().objectName();
            }
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs
                            .builder()
                            .bucket(rootBucketName)
                            .prefix(rootDir)
                            .build());
            for (Result<Item> item:results) {
                try {
                    log.info("item name -> {}", item.get().objectName());
                    if (item.get().isDir()) {
                        directories.add(item.get());
                        allItems.add(item.get());
                    }
                    if(isNotFakeDir(item)) {
                        allItems.add(item.get());
                    }
                } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                         InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                         XmlParserException e) {
                    throw new StorageErrorException("Storage server error");
                }
            }

        } while (!directories.isEmpty() && include);

        return allItems.isEmpty() ? null : allItems;
    }

    private static boolean isNotFakeDir(Result<Item> item) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return !item.get().isDir() && !item.get().objectName().endsWith("/");
    }

    private static String checkAndMakeStringEndingWithSlash(String string) {
        if (!string.endsWith("/")) {
            string = string + "/";
        }
        return string;
    }

    public InputStream downloadFile(String fullPath) throws StorageErrorException {
        try {
            return minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(rootBucketName)
                    .object(fullPath)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageErrorException("Storage server error");
        }
    }
}
