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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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

        InputStream inputStream = null;
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

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(rootBucketName)
                        .prefix(fullPath)
                        .build());

        return results;
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
        log.info("destPath -> {}", destPath);
        log.info("sourcePath -> {}", sourcePath);
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
