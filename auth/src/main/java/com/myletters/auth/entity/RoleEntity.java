package com.myletters.auth.entity;

import com.myletters.auth.config.shared.RoleEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleEntity extends BaseEntity{

    @Enumerated(EnumType.STRING)
    private RoleEnum roleName;

}
