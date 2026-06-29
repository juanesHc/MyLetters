package com.myletters.notes.dto.request;

public record UpdateNoteTitleRequestDto(String title,
                                        String noteId,
                                        String ownerId) {
}
