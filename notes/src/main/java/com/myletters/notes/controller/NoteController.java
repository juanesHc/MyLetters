package com.myletters.notes.controller;

import com.myletters.notes.dto.response.*;
import com.myletters.notes.dto.request.*;
import com.myletters.notes.service.DropNoteService;
import com.myletters.notes.service.RegisterNoteService;
import com.myletters.notes.service.RetrieveNotesService;
import com.myletters.notes.service.UpdateNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final RegisterNoteService registerNoteService;
    private final UpdateNoteService updateNoteService;
    private final DropNoteService dropNoteService;
    private final RetrieveNotesService retrieveNotesService;

    @PostMapping("/register/title")
    public ResponseEntity<CreateNoteResponseDto> registerNoteTitle(@Valid @RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                   @AuthenticationPrincipal String ownerId){
        CreateNoteRequestDto createNoteRequestDto=new CreateNoteRequestDto(getNoteTitleRequestDto.title(),ownerId);
        return ResponseEntity.ok(registerNoteService.createNote(createNoteRequestDto));
    }

    @PatchMapping("/update/title/{noteId}")
    public ResponseEntity<UpdateNoteTitleResponseDto> updateNoteTitle(@Valid @RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                                  @PathVariable String noteId,
                                                                      @AuthenticationPrincipal String ownerId){
        UpdateNoteTitleRequestDto updateNoteRequestDto=new UpdateNoteTitleRequestDto(getNoteTitleRequestDto.title(),noteId,ownerId);
        return ResponseEntity.ok(updateNoteService.updateNoteTitle(updateNoteRequestDto));
    }

    @PostMapping("/register/content/{noteId}")
    public void registerNoteContent(@PathVariable String noteId,
                                    @Valid @RequestBody GetNoteContentRequestDto getNoteContentRequestDto,
                                    @AuthenticationPrincipal String ownerId) {
        RegisterNoteContentRequestDto registerNoteContentRequestDto=new RegisterNoteContentRequestDto(noteId,
                getNoteContentRequestDto.content(), ownerId);
        registerNoteService.registerNoteContent(registerNoteContentRequestDto);
    }

    @DeleteMapping("/delete/note/{noteId}")
    public ResponseEntity<DeleteNoteResponseDto> deleteNote(@PathVariable String noteId , @AuthenticationPrincipal String ownerId ){
        return ResponseEntity.ok(dropNoteService.dropNote(UUID.fromString(noteId), UUID.fromString(ownerId)));
    }

    @GetMapping("/retrieve/note")
    public ResponseEntity<RetrieveAllNotesResponseDto> getAllNotes(@AuthenticationPrincipal String ownerId){
        return ResponseEntity.ok(retrieveNotesService.retrieveAllNotes(UUID.fromString(ownerId)));
    }


}
