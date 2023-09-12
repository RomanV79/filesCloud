package ru.vlasov.fileclouds.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

        if (!path.endsWith("/") && !path.isEmpty()) {
            path = path + "/";
        }
        path = path + ".";

        String objectValue = getRootFolder() + "/" + path;
        log.info("Create folder, objectValue -> {}", objectValue);

        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(rootBucketName)
                .object(objectValue)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    public void createRootUserFolder(String folder) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        if (!folder.endsWith("/")) {
            folder = folder + "/";
        }

        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(rootBucketName)
                .object(folder)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    public List<StorageDto> getFilesAndDirectories(String directory) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        log.info("Root folder -> {}", getRootFolder());
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(rootBucketName)
                        .prefix(getRootFolder() +"/" + directory)
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

    private String getRootFolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails appUserDetails = (AppUserDetails) authentication.getPrincipal();
        return "user-" + appUserDetails.getAppUser().getId() + "-files";
    }
}
