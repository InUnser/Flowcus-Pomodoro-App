package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.PomodoroSessionDto;
import com.flowcus.flowcus.dto.TimerDisplayDto;
import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.service.PomodoroSessionService;
import com.flowcus.flowcus.service.UserService;
import com.flowcus.flowcus.service.UserSettingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/sessions")

public class SessionController {

    private final UserService userService;
    private final UserSettingService userSettingService;
    private final PomodoroSessionService sessionService;

    @Autowired
    public SessionController(UserService userService, UserSettingService userSettingService, PomodoroSessionService sessionService) {
        this.userService = userService;
        this.userSettingService = userSettingService;
        this.sessionService = sessionService;
    }

    @GetMapping("/new")
    public String showAddSessionPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        // Redirect if there is a active pomodoro session
        Optional<String> redirectUrl = sessionService.getActiveSessionRedirectUrl(currentUser.getId());
        if (redirectUrl.isPresent()) {
            return "redirect:" + redirectUrl.get();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionDto", userSettingService.getDefaultSessionDto(currentUser.getId()));

        return "session-new";
    }

    @PostMapping("/start")
    public String startPomodoroSession(@Valid @ModelAttribute("sessionDto") PomodoroSessionDto sessionDto,
                                       BindingResult result,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        if (result.hasErrors()) {
            return "session-new";
        }

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());

        // Redirect if there is a active pomodoro session
        Optional<String> redirectUrl = sessionService.getActiveSessionRedirectUrl(currentUser.getId());
        if (redirectUrl.isPresent()) {
            return "redirect:" + redirectUrl.get();
        }
        PomodoroSession savedSession = sessionService.createSession(currentUser.getId(), sessionDto);

        return "redirect:/sessions/timer/" + savedSession.getId();
    }

    @GetMapping("/timer/{id}")
    public String showTimerPage(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        TimerDisplayDto timerData = sessionService.getTimerDisplayData(id, currentUser.getId());

        if (timerData == null) {
            return "redirect:/past-sessions";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionId", id);

        // Unpack the DTO to match what HTML is looking for
        model.addAttribute("sessionDto", timerData);
        model.addAttribute("currentFocusCount", timerData.getCurrentFocusCount());
        model.addAttribute("targetFocusCount", timerData.getTargetFocusCount());
        model.addAttribute("currentPhaseEnum", timerData.getCurrentPhaseEnum());
        model.addAttribute("phaseLabel", timerData.getPhaseLabel());
        model.addAttribute("themeColor", timerData.getThemeColor());
        model.addAttribute("durationMinutes", timerData.getDurationMinutes());
        model.addAttribute("nextPhaseValue", timerData.getNextPhaseValue());
        model.addAttribute("nextPhaseLabel", timerData.getNextPhaseLabel());

        return "session-timer";
    }

    @PostMapping("/transition/{id}")
    public String transitionPhase(@PathVariable Long id,
                                  @RequestParam("nextPhase") SessionPhase nextPhase,
                                  @RequestParam(value = "actualDuration", defaultValue = "0") int actualDuration,
                                  @RequestParam(value = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        boolean success = sessionService.transitionPhase(id, currentUser.getId(), nextPhase, startTime, actualDuration);

        return success ? "redirect:/sessions/timer/" + id : "redirect:/home";
    }

    @PostMapping("/end/{id}")
    public String endSession(@PathVariable Long id,
                             @RequestParam(value = "actualDuration", defaultValue = "0") int actualDuration,
                             @RequestParam(value = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                             @AuthenticationPrincipal UserDetails userDetails) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        sessionService.endSession(id, currentUser.getId(), startTime, actualDuration);

        return "redirect:/home";
    }
}