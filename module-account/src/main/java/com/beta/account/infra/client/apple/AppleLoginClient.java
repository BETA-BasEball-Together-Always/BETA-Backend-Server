package com.beta.account.infra.client.apple;

import com.beta.account.application.dto.SocialProvider;
import com.beta.account.infra.client.SocialLoginClient;
import com.beta.account.infra.client.SocialUserInfo;
import com.beta.core.exception.account.InvalidAppleTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AppleLoginClient implements SocialLoginClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final long KEY_CACHE_DURATION_MS = 24 * 60 * 60 * 1000;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String appleClientId;
    private final String appleJwksUrl;
    private volatile KeyCache keyCache = KeyCache.EMPTY;

    public AppleLoginClient(WebClient webClient,
                            ObjectMapper objectMapper,
                            @Value("${oauth.apple.client-id}") String appleClientId,
                            @Value("${oauth.apple.jwks-url}") String appleJwksUrl) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.appleClientId = appleClientId;
        this.appleJwksUrl = appleJwksUrl;
    }

    @Override
    public SocialProvider supportedProvider() {
        return SocialProvider.APPLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String idToken) {
        try {
            String kid = extractKid(idToken);
            PublicKey publicKey = getPublicKey(kid);

            String subject = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(appleClientId)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload()
                    .getSubject();

            if (subject == null || subject.isEmpty()) {
                throw new InvalidAppleTokenException("Apple IdToken에 subject가 없습니다");
            }

            return SocialUserInfo.builder().socialId(subject).build();
        } catch (InvalidAppleTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple IdToken 검증 실패: {}", e.getMessage(), e);
            throw new InvalidAppleTokenException("Apple IdToken 검증에 실패했습니다", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractKid(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new InvalidAppleTokenException("유효하지 않은 JWT 형식입니다");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) header.get("kid");

            if (kid == null || kid.isEmpty()) {
                throw new InvalidAppleTokenException("JWT 헤더에 kid가 없습니다");
            }
            return kid;
        } catch (InvalidAppleTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidAppleTokenException("JWT 헤더 파싱 실패", e);
        }
    }

    private PublicKey getPublicKey(String kid) {
        KeyCache cache = this.keyCache;

        if (cache.isExpired()) {
            cache = refreshPublicKeys();
        }

        PublicKey key = cache.get(kid);
        if (key != null) {
            return key;
        }

        cache = refreshPublicKeys();
        key = cache.get(kid);
        if (key == null) {
            throw new InvalidAppleTokenException("Apple 공개키를 찾을 수 없습니다: " + kid);
        }
        return key;
    }

    private KeyCache refreshPublicKeys() {
        try {
            ApplePublicKeyResponse response = webClient.get()
                    .uri(appleJwksUrl)
                    .retrieve()
                    .bodyToMono(ApplePublicKeyResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response == null || response.getKeys() == null || response.getKeys().isEmpty()) {
                throw new InvalidAppleTokenException("Apple 공개키 응답이 비어있습니다");
            }

            Map<String, PublicKey> keys = new HashMap<>();
            for (ApplePublicKeyResponse.Key jwk : response.getKeys()) {
                keys.put(jwk.getKid(), toRsaPublicKey(jwk));
            }

            KeyCache newCache = new KeyCache(Map.copyOf(keys), System.currentTimeMillis());
            this.keyCache = newCache;
            return newCache;
        } catch (InvalidAppleTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 공개키 조회 실패: {}", e.getMessage(), e);
            throw new InvalidAppleTokenException("Apple 공개키 조회에 실패했습니다", e);
        }
    }

    private PublicKey toRsaPublicKey(ApplePublicKeyResponse.Key jwk) {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getN()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getE()));
            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception e) {
            throw new InvalidAppleTokenException("Apple 공개키 생성 실패", e);
        }
    }

    private record KeyCache(Map<String, PublicKey> keys, long fetchedAt) {
        static final KeyCache EMPTY = new KeyCache(Map.of(), 0);

        boolean isExpired() {
            return System.currentTimeMillis() - fetchedAt > KEY_CACHE_DURATION_MS;
        }

        PublicKey get(String kid) {
            return keys.get(kid);
        }
    }
}
