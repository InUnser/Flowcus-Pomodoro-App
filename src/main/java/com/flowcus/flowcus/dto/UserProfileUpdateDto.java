package com.flowcus.flowcus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDto {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    // optional (No @NotBlank)
    // allow the user to skip changing their password.
    private String newPassword;
    private String confirmNewPassword;

    @NotBlank(message = "Profile visibility is required")
    @Pattern(regexp = "^(Public|Private)$", message = "Profile visibility must be either Public or Private")
    private String profileVisibility;

    public boolean isPublicProfile() {
        return "Public".equalsIgnoreCase(profileVisibility);
    }
}
