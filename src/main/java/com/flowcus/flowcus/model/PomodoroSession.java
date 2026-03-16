package com.flowcus.flowcus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pomodoro_sessions")
@Data
public class PomodoroSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @NotBlank(message = "Session title cannot be empty")
    @Column(name="title", nullable = false)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The configuration used for this specific session
    @Column(name = "focus_duration", nullable = false)
    private Integer focusDuration;

    @Column(name = "short_break_duration", nullable = false)
    private Integer shortBreakDuration;

    @Column(name = "long_break_duration", nullable = false)
    private Integer longBreakDuration;

    @Column(name = "sessions_before_long_break", nullable = false)
    private Integer sessionsBeforeLongBreak;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    private SessionPhase currentPhase = SessionPhase.FOCUS;

    // Status of session, defaults to ACTIVE when a new session is created.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    // One session contains many intervals
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PomodoroInterval> intervals = new ArrayList<>();
}

