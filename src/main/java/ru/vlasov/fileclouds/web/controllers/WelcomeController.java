package ru.vlasov.fileclouds.web.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    @GetMapping("/greeting")
    public String greeting(Authentication authentication) {

        String userName = authentication.getName();

        return "Spring Security In-memory Authentication Example - Welcome " + userName;
    }

    @GetMapping("/")
    public String welcome(){
        return "welcome";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
