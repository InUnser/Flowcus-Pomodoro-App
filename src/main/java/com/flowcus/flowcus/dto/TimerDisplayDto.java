package com.flowcus.flowcus.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimerDisplayDto {
    private String title;
    private int focusDuration;
    private int shortBreakDuration;
    private int longBreakDuration;

    private long currentFocusCount;
    private int targetFocusCount;

    private String currentPhaseEnum;
    private String phaseLabel;
    private String themeColor;
    private int durationMinutes;

    private String nextPhaseValue;
    private String nextPhaseLabel;
}
