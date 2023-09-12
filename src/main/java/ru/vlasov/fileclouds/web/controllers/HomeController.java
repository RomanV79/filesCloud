package ru.vlasov.fileclouds.web.controllers;

import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vlasov.fileclouds.config.security.AppUserDetails;
import ru.vlasov.fileclouds.service.FileStorageService;
import ru.vlasov.fileclouds.web.dto.StorageDto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/home")
public class HomeController {

    private final FileStorageService storageService;

    @Autowired
    public HomeController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public String home(@RequestParam(value = "path", required = false) String path, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);

        AppUserDetails appUserDetails = null;
        if (isAuthenticated) {
            appUserDetails = (AppUserDetails) authentication.getPrincipal();
            log.info("User -> {}", appUserDetails.getAppUser());
        }

        log.info("User isAuthenticated -> {}", isAuthenticated);

        if (path == null) {
            path = "";
        }
        log.info("Path -> {}", path);
        List<StorageDto> storageDtoList = null;
        try {
            storageDtoList = storageService.getFilesAndDirectories(path);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("storageList", storageDtoList);

        return "home";
    }
}
