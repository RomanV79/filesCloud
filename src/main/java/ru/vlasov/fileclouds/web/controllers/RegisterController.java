package ru.vlasov.fileclouds.web.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.service.AppUserServiceImpl;
import ru.vlasov.fileclouds.web.dto.UserDto;

@Slf4j
@Controller
public class RegisterController {

    private final AppUserServiceImpl userService;

    public RegisterController(AppUserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
//        AppUser appUserExisting = userService.findUserByEmail(userDto.getEmail());

//        if(existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()){
//            result.rejectValue("email", null,
//                    "There is already an account registered with the same email");
//        }

//        if(result.hasErrors()){
//            model.addAttribute("user", userDto);
//            return "/register";
//        }

        try {
            userService.save(userDto);
        } catch (UserExistException e) {
            model.addAttribute("errorLogin", e.getMessage());
            return "register";
        }
//        return "redirect:/register?success";
        return "redirect:/home";
    }
}
