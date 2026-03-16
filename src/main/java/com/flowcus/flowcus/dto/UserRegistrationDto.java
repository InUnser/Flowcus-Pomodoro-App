package com.flowcus.flowcus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    // Matches the Radio Button: public or private, by default put public so the form can select the radio button
    @NotBlank(message = "Profile visibility is required")
    @Pattern(regexp = "^(Public|Private)$", message = "Profile visibility must be either Public or Private")
    private String profileVisibility="Public";

    public boolean isPublicProfile() {
        return "Public".equalsIgnoreCase(profileVisibility);
    }

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
