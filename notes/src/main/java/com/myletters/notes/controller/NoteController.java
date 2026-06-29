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

    @PostMapping
    public ResponseEntity<CreateNoteResponseDto> registerNoteTitle(@Valid @RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                   @AuthenticationPrincipal String ownerId){
        CreateNoteRequestDto createNoteRequestDto=new CreateNoteRequestDto(getNoteTitleRequestDto.title(),ownerId);
        return ResponseEntity.ok(registerNoteService.createNote(createNoteRequestDto));
    }

    @PatchMapping("/{noteId}/title")
    public ResponseEntity<UpdateNoteTitleResponseDto> updateNoteTitle(@Valid @RequestBody GetNoteTitleRequestDto getNoteTitleRequestDto,
                                                                                  @PathVariable String noteId,
                                                                      @AuthenticationPrincipal String ownerId){
        UpdateNoteTitleRequestDto updateNoteRequestDto=new UpdateNoteTitleRequestDto(getNoteTitleRequestDto.title(),noteId,ownerId);
        return ResponseEntity.ok(updateNoteService.updateNoteTitle(updateNoteRequestDto));
    }

    @PatchMapping("/{noteId}/content")
    public void registerNoteContent(@PathVariable String noteId,
                                    @Valid @RequestBody GetNoteContentRequestDto getNoteContentRequestDto,
                                    @AuthenticationPrincipal String ownerId) {
        RegisterNoteContentRequestDto registerNoteContentRequestDto=new RegisterNoteContentRequestDto(noteId,
                getNoteContentRequestDto.content(), ownerId);
        registerNoteService.registerNoteContent(registerNoteContentRequestDto);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<DeleteNoteResponseDto> deleteNote(@PathVariable String noteId , @AuthenticationPrincipal String ownerId ){
        return ResponseEntity.ok(dropNoteService.dropNote(UUID.fromString(noteId), UUID.fromString(ownerId)));
    }

    /**
     * Consulta unificada: GET /api/v1/notes lista todas mis notas; GET /api/v1/notes?title=x
     * filtra por título. El title es opcional, así que un solo endpoint cubre ambos casos.
     */
    @GetMapping
    public ResponseEntity<RetrieveAllNotesResponseDto> getNotes(@RequestParam(required = false) String title,
                                                                @AuthenticationPrincipal String ownerId){
        RetrieveNotesFilterRequestDto filter = new RetrieveNotesFilterRequestDto(title);
        return ResponseEntity.ok(retrieveNotesService.searchNotes(UUID.fromString(ownerId), filter));
    }


}
