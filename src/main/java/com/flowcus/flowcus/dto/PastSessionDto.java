package com.flowcus.flowcus.dto;

import lombok.Data;

/**
 * DTO designed specifically for the Past Sessions page.
 */
@Data
public class PastSessionDto {
    private Long id;
    private String formattedDateTime; // eg: "1/1/2026 4:00pm"
    private String title;
    private boolean active;
    private int totalFocusMinutes;
    private int totalShortBreakMinutes;
    private int totalLongBreakMinutes;

    private String formatTime(int minutes, String type, boolean includeTotalPrefix) {
        String prefix = includeTotalPrefix ? "Total " : "";
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins > 0) {
                return prefix + hours + " hour " + mins + " min " + type;
            }
            return prefix + hours + " hour " + type;
        }
        return prefix + minutes + " min " + type;
    }

    public String getFormattedFocusTime() {
        return formatTime(totalFocusMinutes, "focus", true);
    }

    public String getFormattedShortBreakTime() {
        return formatTime(totalShortBreakMinutes, "short break", false);
    }

    public String getFormattedLongBreakTime() {
        return formatTime(totalLongBreakMinutes, "long break", false);
    }
}
