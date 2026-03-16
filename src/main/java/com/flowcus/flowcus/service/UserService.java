package com.flowcus.flowcus.service;

import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.dto.UserProfileUpdateDto;
import com.flowcus.flowcus.dto.UserRegistrationDto;
import com.flowcus.flowcus.model.User;
import com.flowcus.flowcus.model.UserSetting;
import com.flowcus.flowcus.repository.UserRepository;
import com.flowcus.flowcus.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserSettingRepository userSettingRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSettingRepository = userSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerNewUser(UserRegistrationDto dto) throws Exception {
        // Check for duplicate username/email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("An account with this email already exists.");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("This username is already taken.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPublic(dto.isPublicProfile());

        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        // Create and link default Settings for the new user
        UserSetting defaultSettings = new UserSetting();
        defaultSettings.setUser(savedUser);
        userSettingRepository.save(defaultSettings);

        return savedUser;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public UserDisplayDto getUserDisplayDtoByEmail(String email) {
        User user = getUserByEmail(email);
        return convertToDisplayDto(user);
    }

    private UserDisplayDto convertToDisplayDto(User user) {
        return UserDisplayDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePic(user.getProfilePic())
                .isPublic(user.isPublic())
                .build();
    }

    /**
     * For the Search Page: Returns a list of safe user DTOs instead of raw user entity.
     */
    public List<UserDisplayDto> searchUserDisplayDtos(String prefix) {
        return userRepository.findByUsernameStartingWithIgnoreCase(prefix).stream()
                .map(this::convertToDisplayDto)
                .collect(Collectors.toList());
    }

    /**
     * For the Public Profile Page: Safely fetch user by their id.
     */
    public Optional<UserDisplayDto> getUserDisplayDtoById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDisplayDto);
    }

    public UserProfileUpdateDto getUserProfileUpdateDto(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfileVisibility(user.isPublic() ? "Public" : "Private");

        return dto;
    }

    /**
     * Checks if a username is already taken by someone else.
     */
    public boolean isUsernameTakenByOther(String username, Long currentUserId) {
        return userRepository.findByUsername(username)
                .map(user -> !user.getId().equals(currentUserId)) // Returns true if it belongs to a different user
                .orElse(false);
    }

    /**
     * Checks if an email is already taken by someone else.
     */
    public boolean isEmailTakenByOther(String email, Long currentUserId) {
        return userRepository.findByEmail(email)
                .map(user -> !user.getId().equals(currentUserId)) // Returns true if it belongs to a different user
                .orElse(false);
    }

    /**
     * Applies the updates to the User entity and handles password encryption.
     */
    public boolean updateUserProfile(Long userId, UserProfileUpdateDto dto) {
        boolean anyChanges=false;
        User user = userRepository.findById(userId).orElseThrow();

        // Update basic fields if they changed
        if (!user.getUsername().equals(dto.getUsername())) {
            user.setUsername(dto.getUsername());
            anyChanges=true;
        }
        if (!user.getEmail().equals(dto.getEmail())) {
            user.setEmail(dto.getEmail());
            anyChanges=true;
        }

        // Encrypt and apply new password (only if user provided one) and if changed
        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            // Use passwordEncoder.matches() to compare raw text to the database hash
            if (!passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                anyChanges = true;
            }
        }

        // Update visibility if changed
        if (user.isPublic() != dto.isPublicProfile()) {
            user.setPublic(dto.isPublicProfile());
            anyChanges=true;
        }

        if (anyChanges) {
            userRepository.save(user);
        }
        return anyChanges;
    }
}
