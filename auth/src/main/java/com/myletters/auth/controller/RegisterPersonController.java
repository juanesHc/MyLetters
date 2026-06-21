package com.myletters.auth.controller;

import com.myletters.auth.dto.request.RegisterPersonRequestDto;
import com.myletters.auth.dto.response.RegisterPersonResponseDto;
import com.myletters.auth.service.register.RegisterPersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegisterPersonController {

    private final RegisterPersonService registerPersonService;

    @PostMapping("/register")
    public ResponseEntity<RegisterPersonResponseDto> register(
            @Valid @RequestBody RegisterPersonRequestDto request) {

        RegisterPersonResponseDto response = registerPersonService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
