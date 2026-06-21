package com.myletters.auth.dto.response;

import java.util.List;

public record PagedUsersResponseDto(
        List<RetrieveUserResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
