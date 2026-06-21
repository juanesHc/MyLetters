package com.myletters.auth.controller;

import com.myletters.auth.dto.request.UpdateAccountDataRequestDto;
import com.myletters.auth.dto.response.RetrievePersonDataResponseDto;
import com.myletters.auth.service.myaccount.MyAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/my-account")
@RequiredArgsConstructor
public class MyAccountController {

    private final MyAccountService myAccountService;

    @GetMapping
    public ResponseEntity<RetrievePersonDataResponseDto> retrievePersonData(
            @AuthenticationPrincipal String principalId) {

        return ResponseEntity.ok(myAccountService.retrievePersonData(currentPersonId(principalId)));
    }

    @PutMapping
    public ResponseEntity<RetrievePersonDataResponseDto> updateAccountData(
            @AuthenticationPrincipal String principalId,
            @Valid @RequestBody UpdateAccountDataRequestDto request) {

        return ResponseEntity.ok(myAccountService.updateAccountData(currentPersonId(principalId), request));
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal String principalId) {

        myAccountService.deactivateAccount(currentPersonId(principalId));
        return ResponseEntity.noContent().build();
    }

    /**
     * El principal lo coloca el JwtAuthFilter y es el id de la persona (subject del token).
     */
    private UUID currentPersonId(String principalId) {
        return UUID.fromString(principalId);
    }
}
