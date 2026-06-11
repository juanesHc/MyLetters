package com.myletters.auth.config.shared.validation;

import com.myletters.auth.exception.InvalidAccountDataException;

import java.util.regex.Pattern;

/**
 * Reglas de validación compartidas por el registro público y la edición de cuenta.
 * Mantiene un único lugar para las reglas de username / email / password.
 */
public final class PersonDataValidator {

    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final int PASSWORD_MIN_LENGTH = 8;

    // El correo debe ser válido y tener la estructura de Google (@gmail.com)
    private static final Pattern GMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$", Pattern.CASE_INSENSITIVE);

    private PersonDataValidator() {
    }

    public static String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidAccountDataException("El username es obligatorio");
        }

        String trimmed = username.trim();
        if (trimmed.length() < USERNAME_MIN_LENGTH || trimmed.length() > USERNAME_MAX_LENGTH) {
            throw new InvalidAccountDataException(
                    "El username debe tener entre " + USERNAME_MIN_LENGTH
                            + " y " + USERNAME_MAX_LENGTH + " caracteres");
        }

        return trimmed;
    }

    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidAccountDataException("El email es obligatorio");
        }

        String trimmed = email.trim();
        if (!GMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidAccountDataException(
                    "El email no es válido o no tiene la estructura de Google (@gmail.com)");
        }

        return trimmed;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidAccountDataException("El password es obligatorio");
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new InvalidAccountDataException(
                    "El password debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
        }

        return password;
    }
}
