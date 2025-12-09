package com.beta.account.infra.client;

import com.beta.account.application.dto.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialLoginClientFactory {

    private final List<SocialLoginClient> clients;

    public SocialLoginClient getClient(SocialProvider provider) {
        return clients.stream()
                .filter(client -> client.supportedProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + provider));
    }
}
