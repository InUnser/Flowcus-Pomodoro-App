package com.flowcus.flowcus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Table(name = "user_settings")
@Data
public class UserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1) @Max(180)
    @Column(name = "default_focus_minutes")
    private Integer defaultFocusMinutes = 25;

    @Min(1) @Max(30)
    @Column(name = "default_short_break_minutes")
    private Integer defaultShortBreakMinutes = 5;

    @Min(1) @Max(60)
    @Column(name = "default_long_break_minutes")
    private Integer defaultLongBreakMinutes = 15;

    @Min(2) @Max(10)
    @Column(name = "sessions_before_long_break")
    private Integer sessionsBeforeLongBreak = 4;
}
