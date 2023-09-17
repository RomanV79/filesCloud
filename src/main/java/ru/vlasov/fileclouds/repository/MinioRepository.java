package ru.vlasov.fileclouds.repository;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.UploadErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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

    public void uploadFile(String fullPath, @NotNull MultipartFile multipartFile) throws UploadErrorException, BrokenFileException {

        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            throw new BrokenFileException("The file is corrupted and cannot be downloaded");
        }

//        log.info("=============");
//        log.info("bucket - > {} /n", rootBucketName);
//        log.info("fullPath -> {}", fullPath);
//        log.info("filename -> {}", multipartFile.getOriginalFilename());
//        log.info("contentType -> {}", multipartFile.getContentType());
//        log.info("=============");
        try {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs
                    .builder()
                            .bucket(rootBucketName)
                            .object(fullPath + multipartFile.getOriginalFilename())
                            .stream(inputStream, -1,  10485760)
                            .contentType(multipartFile.getContentType())
                    .build());

            log.info("Response Put, headers -> {}", response.headers());
            log.info("Response Put, object -> {}", response.object());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new UploadErrorException("The file was not uploaded, storage server error");
        }
    }
}
