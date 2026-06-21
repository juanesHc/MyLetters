package com.myletters.auth.service.login;

import com.myletters.auth.dto.request.LoginRequestDto;
import com.myletters.auth.dto.response.LoginResponseDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.security.SecurityUser;
import com.myletters.auth.exception.AccountDeactivatedException;
import com.myletters.auth.exception.LoginException;
import com.myletters.auth.repository.PersonRepository;
import com.myletters.auth.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginRequestDto request) {

        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new LoginException("El email y la contraseña son obligatorios");
        }

        PersonEntity person = personRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new LoginException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), person.getPassword())) {
            log.warn("Contraseña incorrecta para el email {}", request.email());
            throw new LoginException("Credenciales inválidas");
        }

        if (!person.isActivated()) {
            throw new AccountDeactivatedException(person.getId());
        }

        String token = jwtService.generateToken(new SecurityUser(person));
        log.info("Login exitoso para la persona {}", person.getId());

        return new LoginResponseDto(token);
    }
}
