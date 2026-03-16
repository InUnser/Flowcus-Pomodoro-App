package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.dto.UserSettingDto;
import com.flowcus.flowcus.service.UserService;
import com.flowcus.flowcus.service.UserSettingService;
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
public class SessionSettingsController {

    private final UserService userService;
    private final UserSettingService userSettingService;

    @GetMapping("/session-settings")
    public String showSettingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());  // no password leak
        model.addAttribute("currentUser", currentUser);

        UserSettingDto dto = userSettingService.getUserSettingDto(currentUser.getId());
        model.addAttribute("settingDto", dto);

        return "session-settings";
    }


    @PostMapping("/session-settings")
    public String saveSettings(@Valid @ModelAttribute("settingDto") UserSettingDto dto,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        // Handle validation errors (if user manually enter using postman)
        if (result.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            return "session-settings";
        }

        userSettingService.updateUserSettings(currentUser.getId(), dto);

        // Flash success message
        redirectAttributes.addFlashAttribute("successMessage", "Settings saved successfully!");
        return "redirect:/session-settings";
    }
}