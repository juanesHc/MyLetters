package com.myletters.notes.dto.response;

import java.util.List;

public record RetrieveAllNotesResponseDto(String message, List<NotesDataResponseDto> notes) {
}
