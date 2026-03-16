package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.service.PomodoroSessionService;
import com.flowcus.flowcus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final PomodoroSessionService sessionService;


    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Use @AuthenticationPrincipal to access the logged-in user stored in the Spring Security session.
     */
    @GetMapping("/home")
    public String showHomePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        Optional<String> redirectUrl = sessionService.getActiveSessionRedirectUrl(currentUser.getId());
        if (redirectUrl.isPresent()) {
            return "redirect:" + redirectUrl.get();
        }
        // Pass the user object to the view so we can show their name/profile pic
        model.addAttribute("currentUser", currentUser);
        return "home";
    }

}