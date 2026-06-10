package com.myletters.auth.config.db;

import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {

            if (!roleRepository.existsByRoleName(roleEnum)) {
                RoleEntity newRole = new RoleEntity();
                newRole.setRoleName(roleEnum);
                roleRepository.save(newRole);

                log.info("roles inserted");
            }
        });
    }
}
