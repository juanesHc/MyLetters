package com.myletters.auth.specification;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.dto.request.RetrieveUsersFilterRequestDto;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.exception.InvalidAccountDataException;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Construye dinámicamente los criterios de búsqueda de usuarios. Cada filtro presente
 * se suma con AND; los ausentes simplemente no restringen la consulta.
 */
public final class UsersSpecification {

    private UsersSpecification() {
    }

    private static Specification<PersonEntity> hasUsername(String username) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }

    private static Specification<PersonEntity> hasEmail(String email) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    }

    private static Specification<PersonEntity> hasRole(RoleEnum role) {
        return (root, query, cb) ->
                cb.equal(root.get("roleEntity").get("roleName"), role);
    }

    private static Specification<PersonEntity> hasProvider(ProviderEnum provider) {
        return (root, query, cb) ->
                cb.equal(root.get("provider"), provider);
    }

    private static Specification<PersonEntity> isActivated(boolean activated) {
        return (root, query, cb) ->
                cb.equal(root.get("activated"), activated);
    }

    private static Specification<PersonEntity> createdOn(LocalDate day) {
        return (root, query, cb) ->
                cb.between(root.get("createdAt"), day.atStartOfDay(), day.atTime(23, 59, 59));
    }

    private static Specification<PersonEntity> createdBetween(LocalDate source, LocalDate target) {
        return (root, query, cb) ->
                cb.between(root.get("createdAt"), source.atStartOfDay(), target.atTime(23, 59, 59));
    }

    public static Specification<PersonEntity> build(RetrieveUsersFilterRequestDto filter) {

        Specification<PersonEntity> spec = Specification.allOf();

        if (hasText(filter.username())) {
            spec = spec.and(hasUsername(filter.username().trim()));
        }
        if (hasText(filter.email())) {
            spec = spec.and(hasEmail(filter.email().trim()));
        }
        if (hasText(filter.roleName())) {
            spec = spec.and(hasRole(parseRole(filter.roleName().trim())));
        }
        if (hasText(filter.provider())) {
            spec = spec.and(hasProvider(parseProvider(filter.provider().trim())));
        }
        if (filter.activated() != null) {
            spec = spec.and(isActivated(filter.activated()));
        }
        if (filter.createdAt() != null) {
            spec = spec.and(createdOn(filter.createdAt()));
        }
        if (filter.sourceDate() != null && filter.targetDate() != null) {
            spec = spec.and(createdBetween(filter.sourceDate(), filter.targetDate()));
        }

        return spec;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static RoleEnum parseRole(String roleName) {
        try {
            return RoleEnum.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidAccountDataException("Rol inválido: " + roleName);
        }
    }

    private static ProviderEnum parseProvider(String provider) {
        try {
            return ProviderEnum.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidAccountDataException("Provider inválido: " + provider);
        }
    }
}
