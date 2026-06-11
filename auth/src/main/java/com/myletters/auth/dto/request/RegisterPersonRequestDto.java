package com.myletters.auth.dto.request;

public record RegisterPersonRequestDto(
        String username,
        String email,
        String password
) {
}
