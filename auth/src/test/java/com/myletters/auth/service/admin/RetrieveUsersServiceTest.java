package com.myletters.auth.service.admin;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.RetrieveUsersFilterRequestDto;
import com.myletters.auth.dto.response.PagedUsersResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.exception.InvalidAccountDataException;
import com.myletters.auth.repository.PersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetrieveUsersService - consulta de usuarios por filtros (ADMIN)")
class RetrieveUsersServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private RetrieveUsersService retrieveUsersService;

    private PersonEntity person() {
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity();
        person.setId(UUID.randomUUID());
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(role);
        return person;
    }

    @Test
    @DisplayName("mapea el contenido y la metadata de paginación")
    void retrieveUsers_mapsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PersonEntity> page = new PageImpl<>(List.of(person()), pageable, 1);

        when(personRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        RetrieveUsersFilterRequestDto filter = new RetrieveUsersFilterRequestDto(
                null, null, null, null, null, null, null, null);

        PagedUsersResponseDto response = retrieveUsersService.retrieveUsers(filter, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).username()).isEqualTo("henry");
        assertThat(response.content().get(0).email()).isEqualTo("henry@gmail.com");
        assertThat(response.content().get(0).role()).isEqualTo(RoleEnum.COMMON);
        assertThat(response.content().get(0).provider()).isEqualTo(ProviderEnum.CLASSIC);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("devuelve página vacía cuando no hay coincidencias")
    void retrieveUsers_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(personRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        RetrieveUsersFilterRequestDto filter = new RetrieveUsersFilterRequestDto(
                "noexiste", null, null, null, null, null, null, null);

        PagedUsersResponseDto response = retrieveUsersService.retrieveUsers(filter, pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("rechaza un roleName inválido con InvalidAccountDataException")
    void retrieveUsers_invalidRole() {
        Pageable pageable = PageRequest.of(0, 10);

        RetrieveUsersFilterRequestDto filter = new RetrieveUsersFilterRequestDto(
                null, null, "SUPERADMIN", null, null, null, null, null);

        assertThatThrownBy(() -> retrieveUsersService.retrieveUsers(filter, pageable))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("rechaza un provider inválido con InvalidAccountDataException")
    void retrieveUsers_invalidProvider() {
        Pageable pageable = PageRequest.of(0, 10);

        RetrieveUsersFilterRequestDto filter = new RetrieveUsersFilterRequestDto(
                null, null, null, "FACEBOOK", null, null, null, null);

        assertThatThrownBy(() -> retrieveUsersService.retrieveUsers(filter, pageable))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}
