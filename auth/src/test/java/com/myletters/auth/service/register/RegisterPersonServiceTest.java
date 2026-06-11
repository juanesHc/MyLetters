package com.myletters.auth.service.register;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.RegisterPersonRequestDto;
import com.myletters.auth.dto.response.RegisterPersonResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.exception.EmailAlreadyExistsException;
import com.myletters.auth.exception.InvalidAccountDataException;
import com.myletters.auth.exception.RoleNotFoundException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterPersonService - registro público (role COMMON)")
class RegisterPersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterPersonService registerPersonService;

    private RegisterPersonRequestDto request;
    private RoleEntity commonRole;

    @BeforeEach
    void setUp() {
        request = new RegisterPersonRequestDto("henry", "henry@gmail.com", "miPassword123");

        commonRole = new RoleEntity();
        commonRole.setRoleName(RoleEnum.COMMON);
    }

    @Test
    @DisplayName("registra correctamente una persona con role COMMON, provider CLASSIC y password encriptado")
    void register_success() {
        when(personRepository.existsByEmail("henry@gmail.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.COMMON)).thenReturn(Optional.of(commonRole));
        when(passwordEncoder.encode("miPassword123")).thenReturn("ENCODED_PWD");
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterPersonResponseDto response = registerPersonService.register(request);

        // se persiste con los valores esperados
        ArgumentCaptor<PersonEntity> captor = ArgumentCaptor.forClass(PersonEntity.class);
        verify(personRepository).save(captor.capture());
        PersonEntity persisted = captor.getValue();

        assertThat(persisted.getUsername()).isEqualTo("henry");
        assertThat(persisted.getEmail()).isEqualTo("henry@gmail.com");
        assertThat(persisted.getPassword()).isEqualTo("ENCODED_PWD");
        assertThat(persisted.getProvider()).isEqualTo(ProviderEnum.CLASSIC);
        assertThat(persisted.getRoleEntity().getRoleName()).isEqualTo(RoleEnum.COMMON);

        // la respuesta refleja los datos guardados y nunca expone el password
        assertThat(response.username()).isEqualTo("henry");
        assertThat(response.email()).isEqualTo("henry@gmail.com");
        assertThat(response.role()).isEqualTo(RoleEnum.COMMON);
        assertThat(response.provider()).isEqualTo(ProviderEnum.CLASSIC);
    }

    @Test
    @DisplayName("lanza EmailAlreadyExistsException si el email ya está registrado y no guarda nada")
    void register_emailAlreadyExists() {
        when(personRepository.existsByEmail("henry@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> registerPersonService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(roleRepository, never()).findByRoleName(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza RoleNotFoundException si no existe el role COMMON y no guarda nada")
    void register_roleNotFound() {
        when(personRepository.existsByEmail("henry@gmail.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.COMMON)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerPersonService.register(request))
                .isInstanceOf(RoleNotFoundException.class);

        verify(personRepository, never()).save(any());
    }

    // ---------- mismas reglas que editar cuenta (username 3-20, email @gmail.com) ----------

    @Test
    @DisplayName("rechaza username con menos de 3 caracteres antes de tocar la BD")
    void register_usernameTooShort() {
        RegisterPersonRequestDto invalid =
                new RegisterPersonRequestDto("ab", "henry@gmail.com", "miPassword123");

        assertThatThrownBy(() -> registerPersonService.register(invalid))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).existsByEmail(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza username con más de 20 caracteres")
    void register_usernameTooLong() {
        RegisterPersonRequestDto invalid =
                new RegisterPersonRequestDto("a".repeat(21), "henry@gmail.com", "miPassword123");

        assertThatThrownBy(() -> registerPersonService.register(invalid))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza email que no tiene la estructura de Google (@gmail.com)")
    void register_nonGmailEmail() {
        RegisterPersonRequestDto invalid =
                new RegisterPersonRequestDto("henry", "henry@hotmail.com", "miPassword123");

        assertThatThrownBy(() -> registerPersonService.register(invalid))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza password con menos de 8 caracteres")
    void register_passwordTooShort() {
        RegisterPersonRequestDto invalid =
                new RegisterPersonRequestDto("henry", "henry@gmail.com", "1234");

        assertThatThrownBy(() -> registerPersonService.register(invalid))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }
}
