package com.myletters.auth.service.register;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.config.shared.validation.PersonDataValidator;
import com.myletters.auth.dto.request.RegisterPersonRequestDto;
import com.myletters.auth.dto.response.RegisterPersonResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.exception.EmailAlreadyExistsException;
import com.myletters.auth.exception.RoleNotFoundException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterPersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterPersonResponseDto register(RegisterPersonRequestDto request) {

        String username = PersonDataValidator.validateUsername(request.username());
        String email = PersonDataValidator.validateEmail(request.email());
        String password = PersonDataValidator.validatePassword(request.password());

        if (personRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        RoleEntity commonRole = roleRepository.findByRoleName(RoleEnum.COMMON)
                .orElseThrow(() -> new RoleNotFoundException(RoleEnum.COMMON));

        PersonEntity person = new PersonEntity();
        person.setUsername(username);
        person.setEmail(email);
        person.setPassword(passwordEncoder.encode(password));
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(commonRole);

        PersonEntity saved = personRepository.save(person);
        log.info("Nueva persona registrada con email {} y role COMMON", saved.getEmail());

        return new RegisterPersonResponseDto(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRoleEntity().getRoleName(),
                saved.getProvider()
        );
    }
}
