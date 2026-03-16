package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.UserRegistrationDto;
import com.flowcus.flowcus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles Login and Registration web requests.
 */
@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(@AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            return "redirect:/home";
        }

        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // Pass an empty DTO for binding
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
                                      BindingResult result,
                                      Model model) {

        // Check for validation errors like email format, password length
        if (result.hasErrors()) {
            return "register";
        }

        // Check if password match with confirm password
        if (!userDto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.userDto", "Passwords do not match!");
            return "register";
        }

        try {
            userService.registerNewUser(userDto);
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            // Catch "Username taken" or "Email exists" errors from UserService
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }
    }
}