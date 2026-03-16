package com.flowcus.flowcus.service;

import com.flowcus.flowcus.dto.PastSessionDto;
import com.flowcus.flowcus.dto.PomodoroSessionDto;
import com.flowcus.flowcus.dto.TimerDisplayDto;
import com.flowcus.flowcus.model.*;
import com.flowcus.flowcus.repository.PomodoroIntervalRepository;
import com.flowcus.flowcus.repository.PomodoroSessionRepository;
import com.flowcus.flowcus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PomodoroSessionService {

    private final PomodoroSessionRepository sessionRepository;
    private final PomodoroIntervalRepository intervalRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy h:mma");

    public Optional<String> getActiveSessionRedirectUrl(Long userId) {
        return sessionRepository.findFirstByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .map(session -> "/sessions/timer/" + session.getId());
    }

    public List<PastSessionDto> getPastSessions(Long userId) {
        List<PomodoroSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return sessions.stream()
                .map(this::convertToPastSessionDto)
                .collect(Collectors.toList());
    }

    /**
     *  Deletes a session only if the user owns it and it's not active.
     */
    public boolean deletePastSession(Long sessionId, Long userId) {
        return sessionRepository.findById(sessionId).map(session -> {
            if (isSessionOwnedAndNotActive(session, userId)) {
                sessionRepository.delete(session);
                return true;
            }
            return false;
        }).orElse(false);
    }

    /**
     * Fetch a session for the Edit Page.
     * Returns null if the user isn't allowed to edit it.
     */
    public PomodoroSessionDto getSessionForEdit(Long sessionId, Long userId) {
        return sessionRepository.findById(sessionId)
                .filter(session -> isSessionOwnedAndNotActive(session, userId))
                .map(session -> {
                    PomodoroSessionDto dto = new PomodoroSessionDto();
                    dto.setTitle(session.getTitle());
                    dto.setFocusSessionsBeforeLongBreak(session.getSessionsBeforeLongBreak());
                    dto.setFocusDuration(session.getFocusDuration());
                    dto.setShortBreakDuration(session.getShortBreakDuration());
                    dto.setLongBreakDuration(session.getLongBreakDuration());
                    return dto;
                }).orElse(null);
    }


    public boolean updateSessionTitle(Long sessionId, Long userId, String newTitle) {
        return sessionRepository.findById(sessionId).map(session -> {
            if (isSessionOwnedAndNotActive(session, userId)) {
                session.setTitle(newTitle);
                sessionRepository.save(session);
                return true;
            }
            return false;
        }).orElse(false);
    }

    /**
     * Security check to ensure a user owns the session and is not active
     */
    private boolean isSessionOwnedAndNotActive(PomodoroSession session, Long userId) {
        return session.getUser().getId().equals(userId) && session.getStatus() != SessionStatus.ACTIVE;
    }

    /**
     * Convert a raw database session into a PastSessionDto for the UI.
     */
    private PastSessionDto convertToPastSessionDto(PomodoroSession session) {
        PastSessionDto dto = new PastSessionDto();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setFormattedDateTime(formatSessionEndTime(session.getCreatedAt(), session.getEndTime()));
        dto.setActive(session.getStatus() == SessionStatus.ACTIVE);

        int focusSum = 0, shortSum = 0, longSum = 0;
        for (PomodoroInterval interval : session.getIntervals()) {
            if (interval.getIntervalType() == IntervalType.FOCUS) {
                focusSum += interval.getDurationMinutes();
            } else if (interval.getIntervalType() == IntervalType.SHORT_BREAK) {
                shortSum += interval.getDurationMinutes();
            } else if (interval.getIntervalType() == IntervalType.LONG_BREAK) {
                longSum += interval.getDurationMinutes();
            }
        }

        dto.setTotalFocusMinutes(focusSum);
        dto.setTotalShortBreakMinutes(shortSum);
        dto.setTotalLongBreakMinutes(longSum);

        return dto;
    }

    public PomodoroSession createSession(Long userId, PomodoroSessionDto dto) {
        User user = userRepository.findById(userId).orElseThrow();

        PomodoroSession session = new PomodoroSession();
        session.setTitle(dto.getTitle());
        session.setFocusDuration(dto.getFocusDuration());
        session.setShortBreakDuration(dto.getShortBreakDuration());
        session.setLongBreakDuration(dto.getLongBreakDuration());
        session.setSessionsBeforeLongBreak(dto.getFocusSessionsBeforeLongBreak());
        session.setUser(user);

        return sessionRepository.save(session);
    }

    public TimerDisplayDto getTimerDisplayData(Long sessionId, Long userId) {
        // Fetch & Secure
        PomodoroSession session = sessionRepository.findById(sessionId)
                .filter(s -> isSessionOwnedAndActive(userId, s))
                .orElse(null);

        if (session == null) return null;

        // Calculate Math
        long currentFocusCount = calculateCurrentFocusCount(session);

        // Build Base DTO
        TimerDisplayDto.TimerDisplayDtoBuilder builder = TimerDisplayDto.builder()
                .title(session.getTitle())
                .focusDuration(session.getFocusDuration())
                .shortBreakDuration(session.getShortBreakDuration())
                .longBreakDuration(session.getLongBreakDuration())
                .currentFocusCount(currentFocusCount)
                .targetFocusCount(session.getSessionsBeforeLongBreak())
                .currentPhaseEnum(session.getCurrentPhase().name());

        // Apply UI-specific labels and colors
        applyPhaseSpecificUiData(builder, session, currentFocusCount);

        return builder.build();
    }

    /**
     * For transition phase eg. focus -> break, break->focus
     */
    public boolean transitionPhase(Long sessionId, Long userId, SessionPhase nextPhase, LocalDateTime startTime, int actualDuration) {
        return processSessionAction(sessionId, userId, startTime, actualDuration,
                session -> session.setCurrentPhase(nextPhase));
    }

    /**
     * For ending the Pomodoro Session
     */
    public boolean endSession(Long sessionId, Long userId, LocalDateTime startTime, int actualDuration) {
        return processSessionAction(sessionId, userId, startTime, actualDuration,
                session -> {
                    session.setStatus(SessionStatus.COMPLETED);
                    session.setEndTime(LocalDateTime.now());
                });
    }


    /**
     * Handles security checks, fetching, and interval saving for any session action.
     */
    private boolean processSessionAction(Long sessionId, Long userId, LocalDateTime startTime, int actualDuration, Consumer<PomodoroSession> sessionModifier) {
        return sessionRepository.findById(sessionId).map(session -> {
            if (isSessionOwnedAndActive(userId, session)) {

                saveCompletedInterval(session, startTime, actualDuration);

                // Execute the specific action passed in param (set phase OR set status or set end time)
                sessionModifier.accept(session);

                sessionRepository.save(session);
                return true;
            }
            return false;
        }).orElse(false);
    }

    private boolean isSessionOwnedAndActive(Long userId, PomodoroSession session) {
        return session.getUser().getId().equals(userId) && session.getStatus() == SessionStatus.ACTIVE;
    }

    private long calculateCurrentFocusCount(PomodoroSession session) {
        long pastFocusCount = intervalRepository.findBySessionIdOrderBySequenceOrderAsc(session.getId())
                .stream()
                .filter(i -> i.getIntervalType() == IntervalType.FOCUS)
                .count();

        return session.getCurrentPhase() == SessionPhase.FOCUS ? pastFocusCount + 1 : pastFocusCount;
    }

    /**
     * Maps the database's session phase to the correct colors and text labels.
     */
    private void applyPhaseSpecificUiData(TimerDisplayDto.TimerDisplayDtoBuilder builder, PomodoroSession session, long currentFocusCount) {
        switch (session.getCurrentPhase()) {
            case FOCUS -> {
                builder.phaseLabel("Focus")
                        .themeColor("green")
                        .durationMinutes(session.getFocusDuration());

                if (currentFocusCount % session.getSessionsBeforeLongBreak() == 0) {
                    builder.nextPhaseValue(SessionPhase.LONG_BREAK.name())
                            .nextPhaseLabel("Long Break");
                } else {
                    builder.nextPhaseValue(SessionPhase.SHORT_BREAK.name())
                            .nextPhaseLabel("Short Break");
                }
            }
            case SHORT_BREAK -> {
                builder.phaseLabel("Short Break")
                        .themeColor("blue")
                        .durationMinutes(session.getShortBreakDuration())
                        .nextPhaseValue(SessionPhase.FOCUS.name())
                        .nextPhaseLabel("Focus");
            }
            case LONG_BREAK -> {
                builder.phaseLabel("Long Break")
                        .themeColor("orange")
                        .durationMinutes(session.getLongBreakDuration())
                        .nextPhaseValue(SessionPhase.FOCUS.name())
                        .nextPhaseLabel("Focus");
            }
        }
    }

    /**
     * Helper method: calculate and save the PomodoroInterval record
     */
    private void saveCompletedInterval(PomodoroSession session, LocalDateTime startTime, int actualDuration) {
        PomodoroInterval interval = new PomodoroInterval();
        interval.setSession(session);
        interval.setIntervalType(IntervalType.valueOf(session.getCurrentPhase().name()));
        interval.setDurationMinutes(actualDuration);

        int nextSequence = intervalRepository.findBySessionIdOrderBySequenceOrderAsc(session.getId()).size() + 1;
        interval.setSequenceOrder(nextSequence);

        if (startTime != null) {
            interval.setStartTime(startTime);
        } else {
            interval.setStartTime(LocalDateTime.now().minusMinutes(actualDuration));
        }

        interval.setEndTime(LocalDateTime.now());

        intervalRepository.save(interval);
    }

    private String formatSessionEndTime(LocalDateTime start, LocalDateTime end) {
        String startDateTimeString = start.format(DATE_FORMATTER).toLowerCase();
        // if session have not ended
        if (end == null) {
            return "Started on " + startDateTimeString + " -- Not Yet Ended ";
        }

        String endDateTimeString = end.format(DATE_FORMATTER).toLowerCase();
        return "Started on " + startDateTimeString + " -- Ended on " + endDateTimeString;

    }
}
