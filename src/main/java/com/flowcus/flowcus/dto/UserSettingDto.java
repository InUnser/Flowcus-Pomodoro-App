package com.flowcus.flowcus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserSettingDto {

    @Min(value = 2, message = "Must have at least 2 focus session before a long break.")
    @Max(value = 10, message = "Cannot exceed 10 sessions before a long break.")
    private Integer sessionsBeforeLongBreak;

    @Min(1) @Max(180)
    private Integer defaultFocusMinutes;

    @Min(1) @Max(30)
    private Integer defaultShortBreakMinutes;

    @Min(1) @Max(60)
    private Integer defaultLongBreakMinutes;
}