package com.myletters.notes.service;

import com.myletters.notes.dto.request.CreateNoteRequestDto;
import com.myletters.notes.dto.request.RegisterNoteContentRequestDto;
import com.myletters.notes.dto.response.CreateNoteResponseDto;
import com.myletters.notes.entity.NoteDocument;
import com.myletters.notes.exception.NoteNotFoundException;
import com.myletters.notes.exception.NoteTitleDuplicated;
import com.myletters.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterNoteService {

    private final NoteRepository noteRepository;

    public CreateNoteResponseDto createNote(CreateNoteRequestDto createNoteRequestDto){

        if(noteRepository.existsByOwnerIdAndTitle(UUID.fromString(createNoteRequestDto.ownerId()),
                createNoteRequestDto.title())){
            throw new NoteTitleDuplicated("El titulo de una nota debe ser unico");
        }

        NoteDocument noteDocument=new NoteDocument();
        noteDocument.setTitle(createNoteRequestDto.title());
        noteDocument.setOwnerId(UUID.fromString(createNoteRequestDto.ownerId()));


        noteRepository.save(noteDocument);
        return new CreateNoteResponseDto("Nota creada de forma exitosa");
    }

    public void registerNoteContent(RegisterNoteContentRequestDto registerNoteContentRequestDto){

        NoteDocument noteDocument=noteRepository.findById(UUID.fromString(registerNoteContentRequestDto.noteId())).
                orElseThrow(()->new NoteNotFoundException(UUID.fromString(registerNoteContentRequestDto.noteId())));

            noteDocument.setContent(registerNoteContentRequestDto.content());
            noteDocument.setUpdatedAt(LocalDateTime.now());

            noteRepository.save(noteDocument);
    }

}
