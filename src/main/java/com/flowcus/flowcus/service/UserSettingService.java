package com.flowcus.flowcus.service;


import com.flowcus.flowcus.dto.PomodoroSessionDto;
import com.flowcus.flowcus.dto.UserSettingDto;
import com.flowcus.flowcus.model.User;
import com.flowcus.flowcus.model.UserSetting;
import com.flowcus.flowcus.repository.UserRepository;
import com.flowcus.flowcus.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    /**
     * Fetches a user's default settings and maps them to a New Session DTO.
     */
    public PomodoroSessionDto getDefaultSessionDto(Long userId) {
        PomodoroSessionDto dto = new PomodoroSessionDto();

        userSettingRepository.findByUserId(userId).ifPresent(settings -> {
            dto.setFocusDuration(settings.getDefaultFocusMinutes());
            dto.setShortBreakDuration(settings.getDefaultShortBreakMinutes());
            dto.setLongBreakDuration(settings.getDefaultLongBreakMinutes());
            dto.setFocusSessionsBeforeLongBreak(settings.getSessionsBeforeLongBreak());
        });

        return dto;
    }

    /**
     * Fetches user settings specifically mapped for the Settings Form.
     * Returns defaults if the user has no settings record yet.
     */
    public UserSettingDto getUserSettingDto(Long userId) {
        UserSetting settings = userSettingRepository.findByUserId(userId).orElse(new UserSetting());

        UserSettingDto dto = new UserSettingDto();
        dto.setSessionsBeforeLongBreak(settings.getSessionsBeforeLongBreak());
        dto.setDefaultFocusMinutes(settings.getDefaultFocusMinutes());
        dto.setDefaultShortBreakMinutes(settings.getDefaultShortBreakMinutes());
        dto.setDefaultLongBreakMinutes(settings.getDefaultLongBreakMinutes());

        return dto;
    }

    /**
     * Maps the incoming DTO data to the database entity and saves it.
     */
    public void updateUserSettings(Long userId, UserSettingDto dto) {
        UserSetting settings = userSettingRepository.findByUserId(userId).orElse(new UserSetting());

        // In case a brand new settings record, link it to the user
        if (settings.getUser() == null) {
            User user = userRepository.findById(userId).orElseThrow();
            settings.setUser(user);
        }

        // Apply new preferences from the UI
        settings.setSessionsBeforeLongBreak(dto.getSessionsBeforeLongBreak());
        settings.setDefaultFocusMinutes(dto.getDefaultFocusMinutes());
        settings.setDefaultShortBreakMinutes(dto.getDefaultShortBreakMinutes());
        settings.setDefaultLongBreakMinutes(dto.getDefaultLongBreakMinutes());

        userSettingRepository.save(settings);
    }
}