package com.beta.controller;

import com.beta.account.application.AccountAppService;
import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.controller.request.EmailLoginRequest;
import com.beta.controller.request.RefreshTokenRequest;
import com.beta.controller.request.SignupCompleteRequest;
import com.beta.controller.request.SocialLoginRequest;
import com.beta.controller.response.DuplicateResponse;
import com.beta.controller.response.LogoutResponse;
import com.beta.controller.response.SocialLoginResponse;
import com.beta.controller.response.TokenResponse;
import com.beta.security.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountAppService accountAppService;

    @PostMapping("/login/{provider}")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @PathVariable("provider") SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        LoginResult result = accountAppService.processSocialLogin(request.getToken(), provider);
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @PostMapping("/login/email")
    public ResponseEntity<SocialLoginResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        LoginResult result = accountAppService.processEmailLogin(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SocialLoginResponse> completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
        LoginResult result = accountAppService.completeSignup(request.toUserDto(), request.getAgreeMarketing(), request.getPersonalInfoRequired(), request.getSocialToken());
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }

    @GetMapping("/nickname/duplicate-check")
    public ResponseEntity<DuplicateResponse> checkNicknameDuplicate(
            @RequestParam("nickname") @NotBlank String nickname) {
        boolean isDuplicate = accountAppService.isNameDuplicate(nickname);
        return ResponseEntity.ok(DuplicateResponse.of(isDuplicate));
    }

    @GetMapping("/email/duplicate-check")
    public ResponseEntity<DuplicateResponse> checkEmailDuplicate(
            @RequestParam("email") @NotBlank String email) {
        boolean isDuplicate = accountAppService.isEmailDuplicate(email);
        return ResponseEntity.ok(DuplicateResponse.of(isDuplicate));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = TokenResponse.from(accountAppService.refreshTokens(request.getRefreshToken()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        accountAppService.logout(userDetails.userId());
        return ResponseEntity.ok(LogoutResponse.success());
    }
}
