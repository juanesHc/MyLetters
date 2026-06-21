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
import com.myletters.auth.exception.RoleNotFoundException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.repository.RoleRepository;
import com.myletters.auth.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    /**
     * Inicia sesión con Google: si el email ya existe lo reutiliza (enlazándolo si venía
     * del registro clásico), y si no existe crea la cuenta con provider GOOGLE.
     */
    @Transactional
    public LoginResponseDto loginOrRegister(GoogleUserDataDto data) {

        if (data.email() == null || data.email().isBlank()) {
            throw new LoginException("Google no proporcionó un email");
        }

        String email = data.email().trim();
        Optional<PersonEntity> existing = personRepository.findByEmail(email);

        PersonEntity person = existing.isPresent()
                ? linkExistingAccount(existing.get())
                : createGooglePerson(data, email);

        String token = jwtService.generateToken(new SecurityUser(person));
        return new LoginResponseDto(token);
    }

    /**
     * El email ya estaba registrado. Si venía del registro clásico, al entrar con Google
     * se le permite y pasa a MIXTO (desde ahora puede autenticarse con ambos métodos).
     */
    private PersonEntity linkExistingAccount(PersonEntity person) {

        if (!person.isActivated()) {
            throw new AccountDeactivatedException(person.getId());
        }

        if (person.getProvider() == ProviderEnum.CLASSIC) {
            person.setProvider(ProviderEnum.MIXTO);
            person.setUpdatedAt(LocalDateTime.now());
            personRepository.save(person);
            log.info("La cuenta {} pasó de CLASSIC a MIXTO al entrar con Google", person.getId());
        }

        return person;
    }

    private PersonEntity createGooglePerson(GoogleUserDataDto data, String email) {

        RoleEntity commonRole = roleRepository.findByRoleName(RoleEnum.COMMON)
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.COMMON));

        PersonEntity person = new PersonEntity(); // activated = true por el constructor
        person.setUsername(resolveUsername(data, email));
        person.setEmail(email);
        person.setPassword(null); // identidad de Google: no maneja contraseña local
        person.setProvider(ProviderEnum.GOOGLE);
        person.setRoleEntity(commonRole);

        PersonEntity saved = personRepository.save(person);
        log.info("Nueva persona registrada vía Google con email {} y role COMMON", saved.getEmail());

        return saved;
    }

    private String resolveUsername(GoogleUserDataDto data, String email) {
        if (data.givenName() != null && !data.givenName().isBlank()) {
            return data.givenName().trim();
        }
        return email.substring(0, email.indexOf('@'));
    }
}
