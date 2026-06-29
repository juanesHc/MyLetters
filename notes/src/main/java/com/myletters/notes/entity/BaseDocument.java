package com.myletters.notes.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Equivalente NoSQL del BaseEntity del microservicio auth.
 *
 * Diferencias clave frente a JPA/SQL:
 *  - El @Id aquí es {@code org.springframework.data.annotation.Id} (de Spring Data), no el de JPA.
 *  - En SQL la base de datos generaba el id; en Mongo lo generamos nosotros en el constructor
 *    con {@link UUID#randomUUID()} antes de guardar el documento.
 *  - No es @Document: es una clase base que NoteDocument hereda. Spring Data Mongo "aplana"
 *    estos campos heredados dentro del documento final.
 */
@Getter
@Setter
public class BaseDocument {

    @Id
    private UUID id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BaseDocument() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
