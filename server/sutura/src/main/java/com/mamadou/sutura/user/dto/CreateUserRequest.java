package com.mamadou.sutura.user.dto;

public record CreateUserRequest(
        String firstName,
        String lastname,
        String phoneNumber,
        String password
) {
}
