package com.myletters.notes.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


/**
 * Índice único compuesto sobre (ownerId, title): Mongo garantiza, a nivel de base de datos, que un
 * mismo usuario no tenga dos notas con el mismo título. Es la garantía REAL de unicidad — cierra la
 * condición de carrera (TOCTOU) que el existsByOwnerIdAndTitle del service, por sí solo, no puede.
 * Dos usuarios distintos SÍ pueden tener el mismo título, porque el ownerId entra en el índice.
 */
@Getter
@Setter
@Document(collection = "notes")
@CompoundIndex(name = "owner_title_unique", def = "{'ownerId': 1, 'title': 1}", unique = true)
public class NoteDocument extends BaseDocument {

    private UUID ownerId;

    private String title;

    private String content;
}
