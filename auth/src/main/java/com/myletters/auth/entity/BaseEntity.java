package com.myletters.auth.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
