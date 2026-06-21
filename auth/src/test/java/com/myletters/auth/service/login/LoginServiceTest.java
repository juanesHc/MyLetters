package com.myletters.auth.service.login;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.LoginRequestDto;
import com.myletters.auth.dto.response.LoginResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.entity.security.SecurityUser;
import com.myletters.auth.exception.AccountDeactivatedException;
import com.myletters.auth.exception.LoginException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.service.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("LoginService - autenticación y emisión de JWT")
class LoginServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginService loginService;

    private PersonEntity existingPerson() {
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity(); // activated = true por el constructor
        person.setId(UUID.randomUUID());
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setPassword("ENCODED_PWD");
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(role);
        return person;
    }

    @Test
    @DisplayName("devuelve un token cuando las credenciales son válidas")
    void login_success() {
        PersonEntity person = existingPerson();

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(person));
        when(passwordEncoder.matches("miPassword123", "ENCODED_PWD")).thenReturn(true);
        when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("TOKEN_JWT");

        LoginResponseDto response =
                loginService.login(new LoginRequestDto("henry@gmail.com", "miPassword123"));

        assertThat(response.token()).isEqualTo("TOKEN_JWT");
    }

    @Test
    @DisplayName("lanza LoginException cuando el email no existe")
    void login_emailNotFound() {
        when(personRepository.findByEmail("noexiste@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loginService.login(new LoginRequestDto("noexiste@gmail.com", "miPassword123")))
                .isInstanceOf(LoginException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("lanza LoginException cuando la contraseña es incorrecta")
    void login_wrongPassword() {
        PersonEntity person = existingPerson();

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(person));
        when(passwordEncoder.matches("mala", "ENCODED_PWD")).thenReturn(false);

        assertThatThrownBy(() ->
                loginService.login(new LoginRequestDto("henry@gmail.com", "mala")))
                .isInstanceOf(LoginException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("lanza AccountDeactivatedException cuando la cuenta está desactivada")
    void login_deactivated() {
        PersonEntity person = existingPerson();
        person.setActivated(false);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(person));
        when(passwordEncoder.matches("miPassword123", "ENCODED_PWD")).thenReturn(true);

        assertThatThrownBy(() ->
                loginService.login(new LoginRequestDto("henry@gmail.com", "miPassword123")))
                .isInstanceOf(AccountDeactivatedException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("lanza LoginException cuando faltan email o contraseña")
    void login_blankFields() {
        assertThatThrownBy(() -> loginService.login(new LoginRequestDto("", "miPassword123")))
                .isInstanceOf(LoginException.class);

        assertThatThrownBy(() -> loginService.login(new LoginRequestDto("henry@gmail.com", "  ")))
                .isInstanceOf(LoginException.class);

        verify(personRepository, never()).findByEmail(any());
    }
}
