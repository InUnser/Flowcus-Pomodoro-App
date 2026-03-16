package com.flowcus.flowcus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro_intervals")
@Data
public class PomodoroInterval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "interval_type", nullable = false)
    private IntervalType intervalType; // FOCUS, SHORT_BREAK, or LONG_BREAK

    @Min(value = 0, message = "Duration cannot be negative")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private PomodoroSession session;
}


