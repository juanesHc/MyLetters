package com.myletters.auth.service.jwt;

import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.security.SecurityUser;
import com.myletters.auth.exception.LoginException;
import com.myletters.auth.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    /**
     * El principal del token es el id de la persona, por eso cargamos por id (no por email).
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        PersonEntity person = personRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new LoginException("Credenciales inválidas"));
        return new SecurityUser(person);
    }
}
