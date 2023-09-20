package ru.vlasov.fileclouds.web.controllers;

import jakarta.servlet.http.HttpSession;
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
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.service.StorageService;
import ru.vlasov.fileclouds.web.dto.Breadcrumbs;
import ru.vlasov.fileclouds.web.dto.StorageDto;
import ru.vlasov.fileclouds.web.dto.UploadDirDto;
import ru.vlasov.fileclouds.web.dto.Util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/home")
public class HomeController {

    private final StorageService storageService;

    @Autowired
    public HomeController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public String home(@RequestParam(value = "path", defaultValue = "", required = false) String path, Model model, HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);

        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        session.setAttribute("path", path);

        Breadcrumbs breadcrumbs;
        if (path.isEmpty()) {
            breadcrumbs = null;
        } else {
            breadcrumbs = Util.getBreadcrumbs(path);
        }

        List<StorageDto> storageDtoList = null;
        try {
            storageDtoList = storageService.getFilesAndDirectories(path);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("storageList", storageDtoList);

        UploadDirDto uploadDirDto = new UploadDirDto();
        model.addAttribute("uploadDirDto", uploadDirDto);

        return "home";
    }
}
