INSERT INTO users (
    id,
    social_id,
    email,
    nickname,
    social_provider,
    status,
    role,
    signup_step,
    created_at,
    updated_at
) VALUES
    (1, 'admin-social-id', 'admin@test.com', 'admin-user', 'KAKAO', 'ACTIVE', 'ADMIN', 'COMPLETED', NOW(), NOW()),
    (2, 'normal-social-id', 'user@test.com', 'normal-user', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW()),
    (3, 'admin-withdrawn-social-id', 'admin-withdrawn@test.com', 'admin-withdrawn', 'KAKAO', 'WITHDRAWN', 'ADMIN', 'COMPLETED', NOW(), NOW()),
    (4, 'admin-suspended-social-id', 'admin-suspended@test.com', 'admin-suspended', 'KAKAO', 'SUSPENDED', 'ADMIN', 'COMPLETED', NOW(), NOW());
