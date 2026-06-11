package com.myletters.auth.entity;

import com.myletters.auth.config.shared.ProviderEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "person")
@Getter
@Setter
@AllArgsConstructor
public class PersonEntity  extends BaseEntity{

    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private boolean activated;

    @Enumerated(EnumType.STRING)
    private ProviderEnum provider;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity roleEntity;

    public PersonEntity() {
        this.activated = true;
    }
}
