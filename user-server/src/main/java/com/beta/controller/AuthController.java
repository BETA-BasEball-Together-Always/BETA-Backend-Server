package com.beta.controller;

import com.beta.account.application.AccountAppService;
import com.beta.account.application.dto.LoginResult;
import com.beta.account.application.dto.SocialProvider;
import com.beta.account.application.dto.UserDto;
import com.beta.controller.request.SignupCompleteRequest;
import com.beta.controller.request.SocialLoginRequest;
import com.beta.controller.response.SocialLoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/signup/complete")
    public ResponseEntity<SocialLoginResponse> completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
        LoginResult result = accountAppService.completeSignup(request.toUserDto(), request.getAgreeMarketing(), request.getPersonalInfoRequired(), request.getSocialToken());
        return ResponseEntity.ok(SocialLoginResponse.ofLoginResult(result));
    }
}
