package com.myletters.auth.service.myaccount;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.UpdateAccountDataRequestDto;
import com.myletters.auth.dto.response.RetrievePersonDataResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.exception.AccountAlreadyDeactivatedException;
import com.myletters.auth.exception.AccountDeactivatedException;
import com.myletters.auth.exception.EmailAlreadyExistsException;
import com.myletters.auth.exception.InvalidAccountDataException;
import com.myletters.auth.exception.PersonNotFoundException;
import com.myletters.auth.repository.PersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("MyAccountService - consultar información de la cuenta")
class MyAccountServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private MyAccountService myAccountService;

    @Test
    @DisplayName("devuelve los datos de la persona cuando existe")
    void retrievePersonData_success() {
        UUID id = UUID.randomUUID();

        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity();
        person.setId(id);
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setPassword("ENCODED_PWD");
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(role);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        RetrievePersonDataResponseDto response = myAccountService.retrievePersonData(id);

        assertThat(response.username()).isEqualTo("henry");
        assertThat(response.email()).isEqualTo("henry@gmail.com");
    }

    @Test
    @DisplayName("lanza PersonNotFoundException cuando la persona no existe")
    void retrievePersonData_notFound() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myAccountService.retrievePersonData(id))
                .isInstanceOf(PersonNotFoundException.class);
    }

    @Test
    @DisplayName("no permite consultar una cuenta desactivada")
    void retrievePersonData_deactivated() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id);
        person.setActivated(false);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> myAccountService.retrievePersonData(id))
                .isInstanceOf(AccountDeactivatedException.class);
    }

    // ---------- updateAccountData ----------

    private PersonEntity existingPerson(UUID id) {
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity();
        person.setId(id);
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setPassword("ENCODED_PWD");
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(role);
        return person;
    }

    @Test
    @DisplayName("actualiza username y email cuando los datos son válidos")
    void updateAccountData_success() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));
        when(personRepository.existsByEmail("nuevo@gmail.com")).thenReturn(false);
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "nuevo@gmail.com");

        RetrievePersonDataResponseDto response = myAccountService.updateAccountData(id, request);

        assertThat(response.username()).isEqualTo("nuevoNombre");
        assertThat(response.email()).isEqualTo("nuevo@gmail.com");
        assertThat(person.getUsername()).isEqualTo("nuevoNombre");
        assertThat(person.getEmail()).isEqualTo("nuevo@gmail.com");
    }

    @Test
    @DisplayName("permite conservar el mismo email sin chequear unicidad")
    void updateAccountData_sameEmail() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("otroNombre", "henry@gmail.com");

        myAccountService.updateAccountData(id, request);

        verify(personRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("lanza PersonNotFoundException si la persona no existe")
    void updateAccountData_notFound() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "nuevo@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(PersonNotFoundException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("no permite editar una cuenta desactivada")
    void updateAccountData_deactivated() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id);
        person.setActivated(false);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "nuevo@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(AccountDeactivatedException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza username con menos de 3 caracteres")
    void updateAccountData_usernameTooShort() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson(id)));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("ab", "nuevo@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza username con más de 20 caracteres")
    void updateAccountData_usernameTooLong() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson(id)));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("a".repeat(21), "nuevo@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza email que no tiene la estructura de Google (@gmail.com)")
    void updateAccountData_nonGmailEmail() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson(id)));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "henry@hotmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza email con formato inválido aunque mencione gmail")
    void updateAccountData_malformedEmail() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson(id)));

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "henry@@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(InvalidAccountDataException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza EmailAlreadyExistsException si el nuevo email ya está en uso")
    void updateAccountData_emailAlreadyExists() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson(id)));
        when(personRepository.existsByEmail("ocupado@gmail.com")).thenReturn(true);

        UpdateAccountDataRequestDto request =
                new UpdateAccountDataRequestDto("nuevoNombre", "ocupado@gmail.com");

        assertThatThrownBy(() -> myAccountService.updateAccountData(id, request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(personRepository, never()).save(any());
    }

    // ---------- deactivateAccount (soft delete) ----------

    @Test
    @DisplayName("desactiva la cuenta poniendo activated = false")
    void deactivateAccount_success() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id); // activated = true por el constructor

        when(personRepository.findById(id)).thenReturn(Optional.of(person));
        when(personRepository.save(any(PersonEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        myAccountService.deactivateAccount(id);

        assertThat(person.isActivated()).isFalse();
        verify(personRepository).save(person);
    }

    @Test
    @DisplayName("lanza PersonNotFoundException si la persona no existe")
    void deactivateAccount_notFound() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myAccountService.deactivateAccount(id))
                .isInstanceOf(PersonNotFoundException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza AccountAlreadyDeactivatedException si la cuenta ya estaba desactivada")
    void deactivateAccount_alreadyDeactivated() {
        UUID id = UUID.randomUUID();
        PersonEntity person = existingPerson(id);
        person.setActivated(false);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> myAccountService.deactivateAccount(id))
                .isInstanceOf(AccountAlreadyDeactivatedException.class);

        verify(personRepository, never()).save(any());
    }
}
