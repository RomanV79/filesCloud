package ru.vlasov.fileclouds.web.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.service.StorageService;

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

    @PostMapping("/delete")
    public String delete(@RequestParam("delete-name") String deleteName, HttpSession session) {
        String path = getPath(session);
        String fullPath = path + deleteName;

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
    public String upload(@RequestParam("file") MultipartFile multipartFile,
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

    private static String getPath(HttpSession session) {
        return (String) session.getAttribute("path");
    }


}
