package com.flowcus.flowcus.controller;

import com.flowcus.flowcus.dto.AnalyticsSummaryDto;
import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.service.AnalyticsService;
import com.flowcus.flowcus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserService userService;
    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public String showAnalytics(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        UserDisplayDto currentUser = userService.getUserDisplayDtoByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        // Handle missing date (Show the picker)
        if (date == null) {
            model.addAttribute("showPicker", true);
            return "analytics";
        }

        model.addAttribute("showPicker", false);
        model.addAttribute("selectedDate", date.format(DateTimeFormatter.ofPattern("d/M/yyyy")));

        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary(currentUser.getId(), date);

        model.addAttribute("hasData", summary.isHasData());
        model.addAttribute("focusSessionCount", summary.getFocusSessionCount());

        if (summary.isHasData()) {
            model.addAttribute("chartItems", summary.getChartItems());
            model.addAttribute("chartLabels", summary.getChartLabels());
            model.addAttribute("chartData", summary.getChartData());
            model.addAttribute("chartColors", summary.getChartColors());
        }

        return "analytics";
    }
}
