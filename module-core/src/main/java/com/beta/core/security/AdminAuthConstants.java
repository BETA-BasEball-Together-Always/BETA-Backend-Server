package com.beta.core.security;

import java.time.Duration;

public final class AdminAuthConstants {

    private AdminAuthConstants() {
    }

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String ADMIN_CLIENT = "ADMIN";

    public static final String REFRESH_TOKEN_NAMESPACE = "admin_refresh_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = REFRESH_TOKEN_NAMESPACE;
    public static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/admin/auth";
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    public static final String REFRESH_TOKEN_TOKEN_KEY_PREFIX = REFRESH_TOKEN_NAMESPACE + ":token:";
    public static final String REFRESH_TOKEN_USER_KEY_PREFIX = REFRESH_TOKEN_NAMESPACE + ":user:";
}
