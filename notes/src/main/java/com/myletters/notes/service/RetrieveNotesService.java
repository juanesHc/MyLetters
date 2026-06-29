package com.myletters.notes.service;

import com.myletters.notes.dto.response.NotesDataResponseDto;
import com.myletters.notes.dto.response.RetrieveAllNotesResponseDto;
import com.myletters.notes.entity.NoteDocument;
import com.myletters.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetrieveNotesService {

    private final NoteRepository noteRepository;

    public RetrieveAllNotesResponseDto retrieveAllNotes(UUID ownerId){

        List<NoteDocument> noteDocumentList = noteRepository.findByOwnerId(ownerId);

        if (noteDocumentList.isEmpty()) {
            return new RetrieveAllNotesResponseDto("No tienes ninguna nota registrada", List.of());
        }

        List<NotesDataResponseDto> notes = noteDocumentList.stream()
                .map(this::toDto)
                .toList();

        return new RetrieveAllNotesResponseDto(null, notes);
    }

    private NotesDataResponseDto toDto(NoteDocument note) {
        NotesDataResponseDto dto = new NotesDataResponseDto();
        dto.setNoteId(note.getId().toString());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        return dto;
    }
}
