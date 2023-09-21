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
import ru.vlasov.fileclouds.web.dto.StorageDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController {

    private final StorageService storageService;

    @Autowired
    public SearchController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    private String search(@RequestParam(value = "query", required = false) String query, Model model, HttpSession session) {
        query = URLDecoder.decode(query, StandardCharsets.UTF_8);
        log.info("query -> {}", query);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);

        List<StorageDto> storageDtoList;
        try {
            storageDtoList = storageService.getFilesAndDirectoriesForQuery(query);
        } catch (StorageErrorException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("storageList", storageDtoList);

        return "search";
    }
}
