package com.myletters.auth.service.myaccount;

import com.myletters.auth.config.shared.validation.PersonDataValidator;
import com.myletters.auth.dto.request.UpdateAccountDataRequestDto;
import com.myletters.auth.dto.response.RetrievePersonDataResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.exception.AccountAlreadyDeactivatedException;
import com.myletters.auth.exception.AccountDeactivatedException;
import com.myletters.auth.exception.EmailAlreadyExistsException;
import com.myletters.auth.exception.PersonNotFoundException;
import com.myletters.auth.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyAccountService {

    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public RetrievePersonDataResponseDto retrievePersonData(UUID personId) {

        PersonEntity person = getActivePerson(personId);

        return new RetrievePersonDataResponseDto(
                person.getUsername(),
                person.getEmail()
        );
    }

    @Transactional
    public RetrievePersonDataResponseDto updateAccountData(UUID personId,
                                                           UpdateAccountDataRequestDto request) {

        PersonEntity person = getActivePerson(personId);

        String username = PersonDataValidator.validateUsername(request.username());
        String email = PersonDataValidator.validateEmail(request.email());

        // Si el email cambia, debe seguir siendo único
        if (!email.equalsIgnoreCase(person.getEmail()) && personRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        person.setUsername(username);
        person.setEmail(email);
        person.setUpdatedAt(LocalDateTime.now());

        PersonEntity updated = personRepository.save(person);

        return new RetrievePersonDataResponseDto(
                updated.getUsername(),
                updated.getEmail()
        );
    }

    /**
     * Desactiva la cuenta (soft delete): es como eliminar la cuenta, pero conservando el registro.
     */
    @Transactional
    public void deactivateAccount(UUID personId) {

        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));

        if (!person.isActivated()) {
            throw new AccountAlreadyDeactivatedException(personId);
        }

        person.setActivated(false);
        person.setUpdatedAt(LocalDateTime.now());

        personRepository.save(person);
    }

    /**
     * Obtiene una persona activa. Una cuenta desactivada se trata como inaccesible
     * (no se puede consultar ni editar), igual que si estuviera eliminada.
     */
    private PersonEntity getActivePerson(UUID personId) {
        PersonEntity person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException(personId));

        if (!person.isActivated()) {
            throw new AccountDeactivatedException(personId);
        }

        return person;
    }
}
