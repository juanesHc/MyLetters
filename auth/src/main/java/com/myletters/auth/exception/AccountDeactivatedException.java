package com.myletters.auth.exception;

import java.util.UUID;

public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException(UUID id) {
        super("La cuenta está desactivada: " + id);
    }
}
