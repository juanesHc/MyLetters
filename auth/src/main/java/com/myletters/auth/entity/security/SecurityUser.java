package com.myletters.auth.entity.security;

import com.myletters.auth.entity.PersonEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final PersonEntity personEntity;

    public UUID getId() {
        return personEntity.getId();
    }

    public PersonEntity getPerson() {
        return personEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                "ROLE_" + personEntity.getRoleEntity().getRoleName().name()));
    }

    @Override
    public String getPassword() {
        return personEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return personEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Una cuenta desactivada (soft delete) no puede autenticarse.
        return personEntity.isActivated();
    }
}
