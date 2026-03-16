package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.dto.UserProfileUpdateDto;
import com.flowcus.flowcus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class UserProfileSettingsController {

    private final UserService userService;

    @GetMapping("/settings")
    public String showProfileSettings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        UserProfileUpdateDto dto = userService.getUserProfileUpdateDto(userDetails.getUsername());
        model.addAttribute("profileDto", dto);

        return "settings";
    }

    @PostMapping("/settings")
    public String updateProfileSettings(@Valid @ModelAttribute("profileDto") UserProfileUpdateDto dto,
                                        BindingResult result,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        // Basic Validation (like @NotBlank)
        if (result.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            return "settings";
        }

        // Uniqueness Checks
        if (userService.isUsernameTakenByOther(dto.getUsername(), currentUser.getId())) {
            result.rejectValue("username", "error.profileDto", "This username is already taken.");
        }

        if (userService.isEmailTakenByOther(dto.getEmail(), currentUser.getId())) {
            result.rejectValue("email", "error.profileDto", "An account with this email already exists.");
        }

        // Password validation (only check if they are trying to change it)
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            if (dto.getNewPassword().length() < 8) {
                result.rejectValue("newPassword", "error.profileDto", "Password must be at least 8 characters");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                result.rejectValue("confirmNewPassword", "error.profileDto", "Passwords do not match!");
            }
        }

        // Final validation check before saving
        if (result.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            return "settings";
        }
        boolean anyChanges=userService.updateUserProfile(currentUser.getId(), dto);

        if (anyChanges) {
            redirectAttributes.addFlashAttribute("successMessage", "Account profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Account profile has no changes!");
        }
        return "redirect:/settings";
    }
}
