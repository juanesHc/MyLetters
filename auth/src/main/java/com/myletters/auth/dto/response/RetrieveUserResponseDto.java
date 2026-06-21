package com.myletters.auth.dto.response;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record RetrieveUserResponseDto(
        UUID id,
        String username,
        String email,
        RoleEnum role,
        ProviderEnum provider,
        boolean activated,
        LocalDateTime createdAt
) {
}
