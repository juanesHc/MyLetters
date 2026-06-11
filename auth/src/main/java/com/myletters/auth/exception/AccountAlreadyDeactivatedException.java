package com.myletters.auth.exception;

import java.util.UUID;

public class AccountAlreadyDeactivatedException extends RuntimeException {

    public AccountAlreadyDeactivatedException(UUID id) {
        super("La cuenta ya está desactivada: " + id);
    }
}
