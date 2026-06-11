package com.myletters.auth.controller;

import com.myletters.auth.dto.request.UpdateAccountDataRequestDto;
import com.myletters.auth.dto.response.RetrievePersonDataResponseDto;
import com.myletters.auth.service.myaccount.MyAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}")
    public ResponseEntity<RetrievePersonDataResponseDto> retrievePersonData(
            @PathVariable UUID id) {

        return ResponseEntity.ok(myAccountService.retrievePersonData(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RetrievePersonDataResponseDto> updateAccountData(
            @PathVariable UUID id,
            @RequestBody UpdateAccountDataRequestDto request) {

        return ResponseEntity.ok(myAccountService.updateAccountData(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAccount(@PathVariable UUID id) {

        myAccountService.deactivateAccount(id);
        return ResponseEntity.noContent().build();
    }
}
