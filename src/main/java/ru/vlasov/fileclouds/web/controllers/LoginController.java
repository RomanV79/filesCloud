package ru.vlasov.fileclouds.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlasov.fileclouds.web.dto.UserDto;

@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String signInView(@ModelAttribute("userDto") UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return "login";
    }

    @PostMapping
    public String signIn(@ModelAttribute("userDto") UserDto userDto) {
        return "redirect:/";
    }
}
