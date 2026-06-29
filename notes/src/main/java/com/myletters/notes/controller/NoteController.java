package com.myletters.notes.controller;

import com.myletters.notes.dto.request.*;
import com.myletters.notes.dto.response.CreateNoteResponseDto;
import com.myletters.notes.dto.response.UpdateNoteTitleResponseDto;
import com.myletters.notes.service.RegisterNoteService;
import com.myletters.notes.service.UpdateNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final RegisterNoteService registerNoteService;
    private final UpdateNoteService updateNoteService;

    @PostMapping("/register/title")
    public ResponseEntity<CreateNoteResponseDto> registerNoteTitle(@RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                   @AuthenticationPrincipal String ownerId){
        CreateNoteRequestDto createNoteRequestDto=new CreateNoteRequestDto(getNoteTitleRequestDto.title(),ownerId);
        return ResponseEntity.ok(registerNoteService.createNote(createNoteRequestDto));
    }

    @PatchMapping("/update/title/{noteId}")
    public ResponseEntity<UpdateNoteTitleResponseDto> updateNoteTitle(@RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                                  @PathVariable String noteId,
                                                                      @AuthenticationPrincipal String ownerId){
        UpdateNoteTitleRequestDto updateNoteRequestDto=new UpdateNoteTitleRequestDto(getNoteTitleRequestDto.title(),noteId,ownerId);
        return ResponseEntity.ok(updateNoteService.updateNoteTitle(updateNoteRequestDto));
    }

    @PostMapping("/register/content/{noteId}")
    public void registerNoteContent(@PathVariable String noteId, @RequestBody GetNoteContentRequestDto getNoteContentRequestDto) {
        RegisterNoteContentRequestDto registerNoteContentRequestDto=new RegisterNoteContentRequestDto(noteId,
                getNoteContentRequestDto.content());
        registerNoteService.registerNoteContent(registerNoteContentRequestDto);
    }


}
