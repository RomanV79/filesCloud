package ru.vlasov.fileclouds.test;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlasov.fileclouds.service.FileStorageService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Controller
public class TestController {

    private final FileStorageService storageService;
    private final MinioClient minioClient;


    @Autowired
    public TestController(FileStorageService storageService, MinioClient minioClient) {
        this.storageService = storageService;
        this.minioClient = minioClient;
    }

    @GetMapping("/test/api/create")
    public String createFolder(Model model) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String path = "/test-1/test-2/test3/";
        storageService.createFolder(path);

        return "test";
    }

    @GetMapping("/test/api/get-list")
    public String getList(Model model) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("Start -> /test/api/get-list");

        String directory = "test-1/test-2/ ";
        storageService.getFilesAndDirectories(directory);

        log.info("End -> /test/api/get-list");

        return "test";
    }
}
