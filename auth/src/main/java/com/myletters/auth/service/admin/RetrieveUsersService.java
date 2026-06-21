package com.myletters.auth.service.admin;

import com.myletters.auth.dto.request.RetrieveUsersFilterRequestDto;
import com.myletters.auth.dto.response.PagedUsersResponseDto;
import com.myletters.auth.dto.response.RetrieveUserResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.specification.UsersSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrieveUsersService {

    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public PagedUsersResponseDto retrieveUsers(RetrieveUsersFilterRequestDto filter, Pageable pageable) {

        Specification<PersonEntity> spec = UsersSpecification.build(filter);
        Page<PersonEntity> page = personRepository.findAll(spec, pageable);

        List<RetrieveUserResponseDto> content = page.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PagedUsersResponseDto(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private RetrieveUserResponseDto toDto(PersonEntity person) {
        return new RetrieveUserResponseDto(
                person.getId(),
                person.getUsername(),
                person.getEmail(),
                person.getRoleEntity().getRoleName(),
                person.getProvider(),
                person.isActivated(),
                person.getCreatedAt()
        );
    }
}
