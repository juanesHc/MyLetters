package com.myletters.notes.service;

import com.myletters.notes.dto.request.UpdateNoteTitleRequestDto;
import com.myletters.notes.dto.response.UpdateNoteTitleResponseDto;
import com.myletters.notes.entity.NoteDocument;
import com.myletters.notes.exception.NoteNotFoundException;
import com.myletters.notes.exception.NoteTitleDuplicated;
import com.myletters.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateNoteService {

    private final NoteRepository noteRepository;

    public UpdateNoteTitleResponseDto updateNoteTitle(UpdateNoteTitleRequestDto updateNoteTitleRequestDto){

        NoteDocument noteDocument=noteRepository .findByIdAndOwnerId(
                        UUID.fromString(updateNoteTitleRequestDto.noteId()),
                        UUID.fromString(updateNoteTitleRequestDto.ownerId()))
                .orElseThrow(() -> new NoteNotFoundException(
                        UUID.fromString(updateNoteTitleRequestDto.noteId())));

        if (!updateNoteTitleRequestDto.title().equals(noteDocument.getTitle()) && noteRepository.existsByOwnerIdAndTitle(UUID.fromString(updateNoteTitleRequestDto.ownerId()), updateNoteTitleRequestDto.title())) {
            throw new NoteTitleDuplicated("El titulo de una nota debe ser unico");
        }

        noteDocument.setTitle(updateNoteTitleRequestDto.title());
        noteRepository.save(noteDocument);

        return new UpdateNoteTitleResponseDto("Titulo actualizado con exito");
    }

}
