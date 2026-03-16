package com.flowcus.flowcus.service;

import com.flowcus.flowcus.dto.AnalyticsSummaryDto;
import com.flowcus.flowcus.dto.ChartItemDto;
import com.flowcus.flowcus.model.IntervalType;
import com.flowcus.flowcus.model.PomodoroInterval;
import com.flowcus.flowcus.repository.PomodoroIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PomodoroIntervalRepository intervalRepository;

    private static final String[] CHART_COLORS = {
            "#FF4136", "#FFDC00", "#39CCCC", "#8FC153",
            "#B10DC9", "#FF851B", "#0074D9", "#F012BE"
    };

    public AnalyticsSummaryDto getAnalyticsSummary(Long userId, LocalDate date) {
        List<PomodoroInterval> allFocusIntervals = intervalRepository
                .findBySessionUserIdAndIntervalType(userId, IntervalType.FOCUS);
        List<PomodoroInterval> dailyIntervals = filterIntervalsByDate(allFocusIntervals, date);

        if (dailyIntervals.isEmpty()) {
            return createEmptySummary();
        }

        Map<String, Integer> groupedDurations = aggregateDurationsByTitle(dailyIntervals);
        int totalSessionCount = dailyIntervals.size();

        return buildSummaryDto(groupedDurations, totalSessionCount);
    }

    private List<PomodoroInterval> filterIntervalsByDate(List<PomodoroInterval> intervals, LocalDate date) {
        return intervals.stream()
                .filter(interval -> interval.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Groups intervals by their session title and sums up their total minutes
     */
    private Map<String, Integer> aggregateDurationsByTitle(List<PomodoroInterval> intervals) {
        return intervals.stream()
                .collect(Collectors.groupingBy(
                        interval -> interval.getSession().getTitle(),
                        Collectors.summingInt(PomodoroInterval::getDurationMinutes)
                ));
    }

    /**
     * Maps the raw aggregated data into the formatted DTOs required by Chart.js
     */
    private AnalyticsSummaryDto buildSummaryDto(Map<String, Integer> groupedData, int focusSessionCount) {
        List<ChartItemDto> chartItems = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> dataValues = new ArrayList<>();
        List<String> backgroundColors = new ArrayList<>();

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : groupedData.entrySet()) {
            String title = entry.getKey();
            int totalMinutes = entry.getValue();
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];

            chartItems.add(new ChartItemDto(title, totalMinutes, formatTime(totalMinutes), color));
            labels.add(title);
            dataValues.add(totalMinutes);
            backgroundColors.add(color);

            colorIndex++;
        }

        return new AnalyticsSummaryDto(true, focusSessionCount, chartItems, labels, dataValues, backgroundColors);
    }

    private AnalyticsSummaryDto createEmptySummary() {
        return new AnalyticsSummaryDto(false, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private String formatTime(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins > 0) {
                return hours + "hour " + mins + "min";
            }
            return hours + "hour";
        }
        return minutes + "min";
    }
}