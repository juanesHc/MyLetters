package com.myletters.auth.dto.request;

public record UpdateAccountDataRequestDto(
        String username,
        String email
) {
}
