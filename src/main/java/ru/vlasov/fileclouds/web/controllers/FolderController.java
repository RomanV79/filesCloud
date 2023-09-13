package ru.vlasov.fileclouds.web.controllers;

import io.minio.errors.*;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vlasov.fileclouds.service.FileStorageService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping("storage/folder/")
public class FolderController {
    private final FileStorageService storageService;

    @Autowired
    public FolderController(FileStorageService storageService) {
        this.storageService = storageService;
    }


    @PostMapping("/create")
    public String create(@RequestParam("folder-name") String folderName, HttpSession session) {

        String createPath = (String) session.getAttribute("path");
        if (createPath != null && !createPath.endsWith("/")) {
            createPath = createPath + "/";
        }
        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }

        String fullPath;
        if (createPath != null) {
            fullPath = createPath + folderName;
        } else {
            fullPath = folderName;
        }

        try {
            storageService.createFolder(fullPath);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | NoSuchAlgorithmException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/home";
    }
}
