package com.myletters.notes.dto.request;

/**
 * Filtros opcionales para buscar notas (equivalente al RetrieveUsersFilterRequestDto del auth).
 * Por ahora solo el título; cualquier campo nulo/vacío se ignora y no restringe la búsqueda.
 * Está pensado para crecer: aquí irías sumando más filtros (rango de fechas, etc.).
 */
public record RetrieveNotesFilterRequestDto(
        String title
) {
}
