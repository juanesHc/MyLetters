package com.myletters.auth.dto.request;

import com.myletters.auth.config.shared.validation.PersonDataValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPersonRequestDto(

        @NotBlank(message = "El username es obligatorio")
        @Size(min = PersonDataValidator.USERNAME_MIN_LENGTH,
                max = PersonDataValidator.USERNAME_MAX_LENGTH,
                message = "El username debe tener entre 3 y 20 caracteres")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Pattern(regexp = PersonDataValidator.GMAIL_REGEX,
                message = "El email no es válido o no tiene la estructura de Google (@gmail.com)")
        String email,

        @NotBlank(message = "El password es obligatorio")
        @Size(min = PersonDataValidator.PASSWORD_MIN_LENGTH,
                message = "El password debe tener al menos 8 caracteres")
        String password
) {
}
