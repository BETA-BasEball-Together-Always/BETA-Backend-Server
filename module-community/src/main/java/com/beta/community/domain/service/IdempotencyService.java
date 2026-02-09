package com.beta.community.domain.service;

import com.beta.community.infra.repository.IdempotencyRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRedisRepository idempotencyRedisRepository;

    public boolean isDuplicatePost(Long userId, String content) {
        String key = "idempotency:post:" + hash(userId + ":" + content);
        return !idempotencyRedisRepository.setIfAbsent(key);
    }

    public boolean isDuplicateComment(Long userId, Long postId, String content) {
        String key = "idempotency:comment:" + hash(userId + ":" + postId + ":" + content);
        return !idempotencyRedisRepository.setIfAbsent(key);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
