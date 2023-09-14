package ru.vlasov.fileclouds.web.controllers;

import io.minio.errors.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vlasov.fileclouds.service.FileStorageService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Controller
@RequestMapping("api/v1/storage/")
public class StorageController {
    private final FileStorageService storageService;

    @Autowired
    public StorageController(FileStorageService storageService) {
        this.storageService = storageService;
    }


    @PostMapping("folder/create")
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
        return "redirect:/home?path=" + createPath;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("delete-name") String deleteName, HttpSession session) {
        String path = (String) session.getAttribute("path");
        log.info("delete-name -> {}", deleteName);
        try {
            storageService.delete(deleteName);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home?path=" + path;
    }

    @PostMapping("/rename")
    public String rename(@RequestParam("old-name") String oldName, @RequestParam("new-name") String newName, HttpSession session) {
        String path = (String) session.getAttribute("path");
        log.info("old-name -> {}", oldName);
        log.info("new-name -> {}", newName);
        log.info("path -> {}", path);

        try {
            storageService.rename(oldName, newName, path);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home?path=" + path;
    }


}
