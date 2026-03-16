package com.flowcus.flowcus.repository;

import com.flowcus.flowcus.model.PomodoroSession;
import com.flowcus.flowcus.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    // to list out past sessions for the user
    List<PomodoroSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    // can help to find the first active session for a user
    Optional<PomodoroSession> findFirstByUserIdAndStatus(Long userId, SessionStatus status);


}
