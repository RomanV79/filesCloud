package ru.vlasov.fileclouds.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlasov.fileclouds.web.dto.UserDto;

@Slf4j
@Controller
@RequestMapping("/sign-up")
public class SignUpController {

    @GetMapping
    public String signUpView(@ModelAttribute("userDto") UserDto userDto) {
        return "signup";
    }

    @PostMapping()
    public String createUser(@ModelAttribute("userDto") UserDto userDto) {
        return "redirect:/";
    }
}
