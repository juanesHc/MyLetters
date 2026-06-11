package com.myletters.auth.repository;

import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    boolean existsByRoleName(RoleEnum roleName);

    Optional<RoleEntity> findByRoleName(RoleEnum roleName);

}
