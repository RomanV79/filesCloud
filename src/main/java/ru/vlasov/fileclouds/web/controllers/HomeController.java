package ru.vlasov.fileclouds.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlasov.fileclouds.SecurityConfig.AppUserDetails;

@Slf4j
@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping
    public String home(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);

        AppUserDetails appUserDetails = null;
        if (isAuthenticated) {
            appUserDetails = (AppUserDetails) authentication.getPrincipal();
            log.info("User -> {}", appUserDetails.getAppUser());
        }

        log.info("User isAuthenticated -> {}", isAuthenticated);
        return "home";
    }
}
