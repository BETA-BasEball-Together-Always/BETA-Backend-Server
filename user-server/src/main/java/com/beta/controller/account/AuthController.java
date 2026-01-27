package com.beta.controller.account;

import com.beta.account.application.AccountAppService;
import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SignupStepResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.controller.account.request.*;
import com.beta.controller.account.response.*;
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
    public ResponseEntity<LogoutResponse> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) LogoutRequest request) {
        accountAppService.logout(userDetails.userId(), request.getDeviceId());
        return ResponseEntity.ok(LogoutResponse.success());
    }

    @PostMapping("/signup/consent")
    public ResponseEntity<SignupStepResponse> processConsent(@Valid @RequestBody ConsentRequest request) {
        SignupStepResult result = accountAppService.processConsent(
                request.getUserId(),
                request.getPersonalInfoRequired(),
                request.getAgreeMarketing()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @PostMapping("/signup/profile")
    public ResponseEntity<SignupStepResponse> processProfile(@Valid @RequestBody ProfileRequest request) {
        SignupStepResult result = accountAppService.processProfile(
                request.getUserId(),
                request.getEmail(),
                request.getNickname()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @PostMapping("/signup/team")
    public ResponseEntity<SignupStepResponse> processTeamSelection(@Valid @RequestBody TeamSelectionRequest request) {
        SignupStepResult result = accountAppService.processTeamSelection(
                request.getUserId(),
                request.getTeamCode()
        );
        return ResponseEntity.ok(SignupStepResponse.from(result));
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<SignupCompleteResponse> completeSignup(@Valid @RequestBody SignupSkipCompleteRequest request) {
        LoginResult result = accountAppService.completeSignup(request.getUserId());
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }

    @PostMapping("/signup/complete-with-info")
    public ResponseEntity<SignupCompleteResponse> completeSignupWithInfo(@Valid @RequestBody SignupCompleteWithInfoRequest request) {
        LoginResult result = accountAppService.completeSignupWithInfo(
                request.getUserId(),
                request.getGender(),
                request.getAge()
        );
        return ResponseEntity.ok(SignupCompleteResponse.from(result));
    }
}
