package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.PastSessionDto;
import com.flowcus.flowcus.dto.PomodoroSessionDto;
import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.service.PomodoroSessionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PastSessionsController {

    private final UserService userService;
    private final PomodoroSessionService sessionService;

    @GetMapping("/past-sessions")
    public String showPastSessions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);  // for displaying header

        List<PastSessionDto> pastSessions = sessionService.getPastSessions(currentUser.getId());
        model.addAttribute("pastSessions", pastSessions);

        return "past-sessions";
    }

    @PostMapping("/sessions/{id}/delete")
    public String deleteSession(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        sessionService.deletePastSession(id, currentUser.getId());

        return "redirect:/past-sessions";
    }

    @GetMapping("/sessions/edit/{id}")
    public String showEditSessionPage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        PomodoroSessionDto dto = sessionService.getSessionForEdit(id, currentUser.getId());

        // If dto returns null, means the user doesn't own it or is active or no such session. SO redirect
        if (dto == null) {
            return "redirect:/past-sessions?error=cannot_edit_active";
        }

        model.addAttribute("sessionDto", dto);
        model.addAttribute("sessionId", id);
        model.addAttribute("currentUser", currentUser);

        return "session-edit";
    }

    @PostMapping("/sessions/edit/{id}")
    public String saveEditedSession(@PathVariable Long id,
                                    @Valid @ModelAttribute("sessionDto") PomodoroSessionDto sessionDto,
                                    BindingResult result,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        if (result.hasErrors()) {
            model.addAttribute("sessionId", id);
            model.addAttribute("currentUser", currentUser);
            return "session-edit";
        }
        sessionService.updateSessionTitle(id, currentUser.getId(), sessionDto.getTitle());

        return "redirect:/past-sessions";
    }
}