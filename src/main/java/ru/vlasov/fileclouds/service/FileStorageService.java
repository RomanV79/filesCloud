package ru.vlasov.fileclouds.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vlasov.fileclouds.web.dto.StorageDto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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

    public void createFolder(String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        minioClient.putObject(PutObjectArgs
                .builder()
                        .bucket(rootBucketName)
                        .object(path)
                        .stream(new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
    }

    public List<StorageDto> getFilesAndDirectories(String directory) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(rootBucketName)
                        .prefix(directory)
                        .build());
        log.info("results -> {}", results);

        List<StorageDto> storageDtoList = new ArrayList<>();
        for (Result<Item> item:results) {
            Item item1 = item.get();
            StorageDto object = new StorageDto();
            object.setName(item1.objectName());
            object.setDir(item1.isDir());
            log.info("object -> {}", object);
            storageDtoList.add(object);
        }

//        results.forEach(result ->
//        {
//            try {
//                Item item = result.get();
//                log.info("Stream; name -> {}", item.objectName());
//            } catch (ErrorResponseException | InsufficientDataException | InvalidKeyException |
//                     InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException |
//                     IOException | InternalException e) {
//                throw new RuntimeException(e);
//            }
//        });

        return storageDtoList;
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
}
