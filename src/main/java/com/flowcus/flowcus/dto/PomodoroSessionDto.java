package com.flowcus.flowcus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PomodoroSessionDto {

    @NotBlank(message = "Please give your session a title")
    @Size(max = 100)
    private String title;

    @Min(2) @Max(10)
    private Integer focusSessionsBeforeLongBreak = 4;

    @Min(1) @Max(180)
    private Integer focusDuration = 25;

    @Min(1) @Max(30)
    private Integer shortBreakDuration = 5;

    @Min(1) @Max(60)
    private Integer longBreakDuration = 15;
}
