package com.myletters.auth.dto.request;

import java.time.LocalDate;

/**
 * Filtros opcionales para consultar usuarios (solo ADMIN). Cualquier campo nulo/vacío se ignora.
 * createdAt filtra por un día puntual; sourceDate + targetDate filtran por rango de fechas.
 */
public record RetrieveUsersFilterRequestDto(
        String username,
        String email,
        String roleName,
        String provider,
        Boolean activated,
        LocalDate createdAt,
        LocalDate sourceDate,
        LocalDate targetDate
) {
}
