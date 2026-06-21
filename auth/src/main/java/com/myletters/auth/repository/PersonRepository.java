package com.myletters.auth.repository;

import com.myletters.auth.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, UUID>,
        JpaSpecificationExecutor<PersonEntity> {

    boolean existsByEmail(String email);

    Optional<PersonEntity> findByEmail(String email);

}
