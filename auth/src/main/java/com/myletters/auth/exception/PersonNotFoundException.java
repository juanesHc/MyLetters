package com.myletters.auth.exception;

import java.util.UUID;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(UUID id) {
        super("No se encontró la persona con id: " + id);
    }
}
