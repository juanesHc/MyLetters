package com.myletters.notes.exception;

import java.util.UUID;

/**
 * Se lanza cuando una nota no existe, O cuando existe pero no pertenece al usuario que la pide.
 * Devolvemos el mismo 404 en ambos casos a propósito: así no le revelamos a un atacante que la
 * nota existe (solo que "no es suya"). Esto es lo que filtrarás por ownerId en tu reto.
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(UUID id) {
        super("No se encontró la nota con id: " + id);
    }
}
