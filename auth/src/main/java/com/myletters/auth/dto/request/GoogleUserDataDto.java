package com.myletters.auth.dto.request;

/**
 * Datos que entrega Google (OAuth2) sobre el usuario autenticado.
 */
public record GoogleUserDataDto(String email, String givenName, String familyName) {
}
