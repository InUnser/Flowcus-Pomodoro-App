package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.PastSessionDto;
import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.service.PomodoroSessionService;
import com.flowcus.flowcus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PeopleController {

    private final UserService userService;
    private final PomodoroSessionService sessionService;

    @GetMapping("/people")
    public String searchPeople(@RequestParam(name = "q", required = false) String query,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        // Pass the current user for the header
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        // If a search query was submitted, fetch the results
        if (query != null && !query.trim().isEmpty()) {
            List<UserDisplayDto> searchResults = userService.searchUserDisplayDtos(query.trim());
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchQuery", query);
        }

        return "people";
    }

    /**
     * View a specific user's profile and past sessions
     */
    @GetMapping("/people/{id}")
    public String viewPersonProfile(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        // Instantly redirect them to their own Past Sessions page if they search themselves
        if (currentUser.getId().equals(id)) {
            return "redirect:/past-sessions";
        }

        model.addAttribute("currentUser", currentUser);

        UserDisplayDto targetUser = userService.getUserDisplayDtoById(id).orElse(null);
        if (targetUser == null) {
            return "redirect:/people"; // Safely redirect if user doesn't exist
        }

        model.addAttribute("targetUser", targetUser);

        if (targetUser.isPublic()) {
            List<PastSessionDto> pastSessions = sessionService.getPastSessions(targetUser.getId());
            model.addAttribute("pastSessions", pastSessions);
        }

        return "person-profile";
    }
}
