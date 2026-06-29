package com.myletters.notes.service;

import com.myletters.notes.dto.request.RetrieveNotesFilterRequestDto;
import com.myletters.notes.dto.response.NotesDataResponseDto;
import com.myletters.notes.dto.response.RetrieveAllNotesResponseDto;
import com.myletters.notes.entity.NoteDocument;
import com.myletters.notes.specification.NotesSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetrieveNotesService {

    private final MongoTemplate mongoTemplate;

    /**
     * Consulta unificada: siempre acota al dueño y aplica los filtros que vengan. Sin filtros,
     * devuelve todas las notas del usuario. El mensaje del caso vacío depende de si se filtró o no.
     */
    public RetrieveAllNotesResponseDto searchNotes(UUID ownerId, RetrieveNotesFilterRequestDto filter){

        Query query = NotesSpecification.build(ownerId, filter);
        List<NoteDocument> noteDocumentList = mongoTemplate.find(query, NoteDocument.class);

        if (noteDocumentList.isEmpty()) {
            String message = hasText(filter.title())
                    ? "No se encontraron notas con esos filtros"
                    : "No tienes ninguna nota registrada";
            return new RetrieveAllNotesResponseDto(message, List.of());
        }

        List<NotesDataResponseDto> notes = noteDocumentList.stream()
                .map(this::toDto)
                .toList();

        return new RetrieveAllNotesResponseDto(null, notes);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private NotesDataResponseDto toDto(NoteDocument note) {
        NotesDataResponseDto dto = new NotesDataResponseDto();
        dto.setNoteId(note.getId().toString());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        return dto;
    }
}
