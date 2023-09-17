package ru.vlasov.fileclouds.web.controllers;

import io.minio.errors.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.service.AppUserServiceImpl;
import ru.vlasov.fileclouds.service.StorageService;
import ru.vlasov.fileclouds.user.AppUser;
import ru.vlasov.fileclouds.web.dto.UserDto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Controller
public class RegisterController {

    private final AppUserServiceImpl userService;
    private final StorageService storageService;

    public RegisterController(AppUserServiceImpl userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {

        try {
            AppUser appUser = userService.save(userDto);
            String rootFolderCurrentUser = "user-" + appUser.getId() + "-files";
            storageService.createRootUserDirectory(rootFolderCurrentUser);
        } catch (UserExistException e) {
            model.addAttribute("errorLogin", e.getMessage());
            return "register";
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidResponseException | InvalidKeyException | InternalException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/home";
    }
}
