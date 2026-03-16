package com.flowcus.flowcus.repository;

import com.flowcus.flowcus.model.IntervalType;
import com.flowcus.flowcus.model.PomodoroInterval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PomodoroIntervalRepository extends JpaRepository<PomodoroInterval, Long> {
    // Allow sessions for a specific user to build the pie chart for analytics.
    List<PomodoroInterval> findBySessionUserIdAndIntervalType(Long userId, IntervalType type);

    // Allows us to fetch all intervals for a specific session in order.
    List<PomodoroInterval> findBySessionIdOrderBySequenceOrderAsc(Long sessionId);
}
