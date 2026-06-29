package com.myletters.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GetNoteTitleRequestDto(

        // @NotBlank: no null, no vacío y no solo espacios. (Para texto casi siempre quieres
        // @NotBlank, no @NotNull: "   " es no-nulo pero igual no sirve como título.)
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String title
) {
}
