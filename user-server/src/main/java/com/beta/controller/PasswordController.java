package com.beta.controller;

import com.beta.account.application.AccountAppService;
import com.beta.controller.request.ResetPasswordRequest;
import com.beta.controller.request.VerifyCodeRequest;
import com.beta.controller.response.PasswordCodeResponse;
import com.beta.controller.response.ResetPasswordResponse;
import com.beta.controller.response.VerifyCodeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final AccountAppService accountAppService;

    @PostMapping("/code")
    public ResponseEntity<PasswordCodeResponse> sendPasswordResetCode(@RequestParam("email") @NotBlank String email) {
        PasswordCodeResponse response = PasswordCodeResponse.success(accountAppService.sendPasswordResetCode(email));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyCodeResponse> verifyPasswordResetCode(@Valid @RequestBody VerifyCodeRequest request) {
        VerifyCodeResponse response = VerifyCodeResponse.success(accountAppService.verifyPasswordResetCode(request.getEmail(), request.getCode()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse response = ResetPasswordResponse.success(accountAppService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword()));
        return ResponseEntity.ok(response);
    }
}
