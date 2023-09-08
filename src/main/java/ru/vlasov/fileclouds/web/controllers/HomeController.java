package ru.vlasov.fileclouds.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlasov.fileclouds.SecurityConfig.AppUserDetails;

@Slf4j
@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping
    public String home() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails appUserDetails = (AppUserDetails)authentication.getPrincipal();
        log.info("User -> {}", appUserDetails.getAppUser());
        return "home";
    }
}
