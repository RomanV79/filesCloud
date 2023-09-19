package ru.vlasov.fileclouds.web.controllers;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.service.StorageService;
import ru.vlasov.fileclouds.web.dto.UploadDirDto;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Controller
@RequestMapping("api/v1/storage/")
public class StorageController {
    private final StorageService storageService;

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("folder/create")
    public String create(@RequestParam("folder-name") String folderName, HttpSession session) {

        String createPath = getPath(session);

        try {
            storageService.createDirectory(createPath + folderName);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home?path=" + createPath;
    }

    @PostMapping("folder/upload")
    public String uploadDir(@RequestParam("directory") MultipartFile[] multipartFiles, HttpSession session) {

        String path = getPath(session);
        log.info("path -> {}", path);
        try {
            storageService.uploadDirectory(path, multipartFiles);
        } catch (BrokenFileException e) {
            throw new RuntimeException(e);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home?path=" + path;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("delete-name") String deleteName, HttpSession session) {
        String path = getPath(session);
        String fullPath = deleteName;

        try {
            storageService.delete(fullPath);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home?path=" + path;
    }

    @PostMapping("/rename")
    public String rename(@RequestParam("old-name") String oldName, @RequestParam("new-name") String newName, HttpSession session) {
        String path = getPath(session);
        log.info("old-name -> {}", oldName);
        log.info("new-name -> {}", newName);
        log.info("path -> {}", path);

        try {
            storageService.rename(oldName, newName, path);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/home?path=" + path;
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("file") MultipartFile multipartFile,
                              RedirectAttributes redirectAttributes, HttpSession session) {
        log.info("Start upload controller ->");
        String path = getPath(session);
        log.info("file name -> {}", multipartFile.getOriginalFilename());
        try {
            storageService.uploadFile(path, multipartFile);
            log.info("Upload service -> OK");
        } catch (BrokenFileException e) {
            throw new RuntimeException(e);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/home?path=" + path;
    }

    @GetMapping(value = "/download")
    public void download(@RequestParam("path") String fullPath, HttpSession session, HttpServletResponse response) {
//        String path = getPath(session);
        log.info("fullPath -> {}", fullPath);
        String nameFile = getNameFromPath(fullPath);

        if (!fullPath.endsWith("/")) {
            try (InputStream stream = storageService.download(fullPath)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + nameFile);
                FileCopyUtils.copy(stream, response.getOutputStream());
            } catch (StorageErrorException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
//            
        }
    }

    private static String getPath(HttpSession session) {
        return (String) session.getAttribute("path");
    }

    private String getNameFromPath(String path) {
        String[] element = path.split("/");
        return element[element.length -1 ];
    }


}
