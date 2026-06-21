package com.myletters.auth.service.google;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.GoogleUserDataDto;
import com.myletters.auth.dto.response.LoginResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.entity.security.SecurityUser;
import com.myletters.auth.exception.AccountDeactivatedException;
import com.myletters.auth.exception.LoginException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.repository.RoleRepository;
import com.myletters.auth.service.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleAuthService - login/registro con Google y regla MIXTO")
class GoogleAuthServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    private PersonEntity person(ProviderEnum provider) {
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity(); // activated = true
        person.setId(UUID.randomUUID());
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setProvider(provider);
        person.setRoleEntity(role);
        return person;
    }

    private GoogleUserDataDto googleData() {
        return new GoogleUserDataDto("henry@gmail.com", "Henry", "Cordoba");
    }

    @Test
    @DisplayName("crea una persona nueva con provider GOOGLE y role COMMON")
    void newGoogleUser_isCreated() {
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleEnum.COMMON)).thenReturn(Optional.of(role));
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("TOKEN_JWT");

        LoginResponseDto response = googleAuthService.loginOrRegister(googleData());

        assertThat(response.token()).isEqualTo("TOKEN_JWT");

        ArgumentCaptor<PersonEntity> captor = ArgumentCaptor.forClass(PersonEntity.class);
        verify(personRepository).save(captor.capture());
        PersonEntity saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo(ProviderEnum.GOOGLE);
        assertThat(saved.getEmail()).isEqualTo("henry@gmail.com");
        assertThat(saved.getUsername()).isEqualTo("Henry");
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.getRoleEntity().getRoleName()).isEqualTo(RoleEnum.COMMON);
    }

    @Test
    @DisplayName("una cuenta CLASSIC existente pasa a MIXTO al entrar con Google")
    void existingClassicUser_becomesMixto() {
        PersonEntity classic = person(ProviderEnum.CLASSIC);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(classic));
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("TOKEN_JWT");

        LoginResponseDto response = googleAuthService.loginOrRegister(googleData());

        assertThat(response.token()).isEqualTo("TOKEN_JWT");
        assertThat(classic.getProvider()).isEqualTo(ProviderEnum.MIXTO);
        verify(personRepository).save(classic);
    }

    @Test
    @DisplayName("una cuenta GOOGLE existente se mantiene en GOOGLE (no se reescribe)")
    void existingGoogleUser_staysGoogle() {
        PersonEntity google = person(ProviderEnum.GOOGLE);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(google));
        when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("TOKEN_JWT");

        LoginResponseDto response = googleAuthService.loginOrRegister(googleData());

        assertThat(response.token()).isEqualTo("TOKEN_JWT");
        assertThat(google.getProvider()).isEqualTo(ProviderEnum.GOOGLE);
        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("una cuenta MIXTO existente se mantiene en MIXTO")
    void existingMixtoUser_staysMixto() {
        PersonEntity mixto = person(ProviderEnum.MIXTO);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(mixto));
        when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("TOKEN_JWT");

        googleAuthService.loginOrRegister(googleData());

        assertThat(mixto.getProvider()).isEqualTo(ProviderEnum.MIXTO);
        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza AccountDeactivatedException si la cuenta existente está desactivada")
    void existingDeactivatedUser_isRejected() {
        PersonEntity classic = person(ProviderEnum.CLASSIC);
        classic.setActivated(false);

        when(personRepository.findByEmail("henry@gmail.com")).thenReturn(Optional.of(classic));

        assertThatThrownBy(() -> googleAuthService.loginOrRegister(googleData()))
                .isInstanceOf(AccountDeactivatedException.class);

        verify(personRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("lanza LoginException si Google no entrega email")
    void missingEmail_isRejected() {
        assertThatThrownBy(() ->
                googleAuthService.loginOrRegister(new GoogleUserDataDto("  ", "Henry", "Cordoba")))
                .isInstanceOf(LoginException.class);

        verify(personRepository, never()).findByEmail(any());
    }
}
