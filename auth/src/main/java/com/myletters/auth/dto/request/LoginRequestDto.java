package com.myletters.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "El password es obligatorio")
        String password
) {
}
