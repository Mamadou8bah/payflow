package com.mamadou.sutura.auth.dto;

import com.mamadou.sutura.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        @Email String email,
        @NotBlank @Size(min = 8) String password,
        Role role,
        boolean twoFactorEnabled
) {
}
