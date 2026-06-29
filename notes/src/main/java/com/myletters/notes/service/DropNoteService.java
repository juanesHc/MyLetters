package com.myletters.notes.service;

import com.myletters.notes.dto.response.DeleteNoteResponseDto;
import com.myletters.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DropNoteService {

    private final NoteRepository noteRepository;

    public DeleteNoteResponseDto dropNote(UUID noteId, UUID ownerId){
        noteRepository.deleteByIdAndOwnerId(noteId, ownerId);
        return new DeleteNoteResponseDto("Nota eliminada con exito");

}


}
