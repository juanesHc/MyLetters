package com.myletters.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GetNoteContentRequestDto(

        @NotBlank(message = "El contenido es obligatorio")
        @Size(max = 10000, message = "El contenido no puede superar los 10000 caracteres")
        String content
) {
}
