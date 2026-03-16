package com.flowcus.flowcus.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO to display user instead of just using the User entity for not leaking password
 */
@Data
@Builder
public class UserDisplayDto {
    private Long id;
    private String username;
    private String email;
    private String profilePic;
    private boolean isPublic;
}