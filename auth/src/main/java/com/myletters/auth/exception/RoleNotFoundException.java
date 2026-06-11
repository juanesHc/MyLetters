package com.myletters.auth.exception;

import com.myletters.auth.config.shared.RoleEnum;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(RoleEnum roleName) {
        super("No se encontró el role: " + roleName);
    }
}
