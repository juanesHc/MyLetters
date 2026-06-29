package com.myletters.notes.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotesDataResponseDto {
    private String noteId;
    private String title;
    private String content;
}
