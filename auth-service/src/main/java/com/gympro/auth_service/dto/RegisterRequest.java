package com.gympro.auth_service.dto;

import com.gympro.auth_service.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    private Role role;   // optional, defaults to MEMBER
}