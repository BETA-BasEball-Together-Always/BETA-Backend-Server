INSERT INTO users (
    id,
    social_id,
    email,
    nickname,
    social_provider,
    status,
    role,
    signup_step,
    favorite_team_code,
    gender,
    age,
    bio,
    withdrawn_at,
    created_at,
    updated_at
) VALUES
    (7, 'user-social-id-7', 'teen10@test.com', 'teen10', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, 'M', 10, NULL, NULL, DATE_SUB(NOW(), INTERVAL 29 MINUTE), NOW()),
    (8, 'user-social-id-8', 'teen19@test.com', 'teen19', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, 'F', 19, NULL, NULL, DATE_SUB(NOW(), INTERVAL 28 MINUTE), NOW()),
    (9, 'user-social-id-9', 'thirty9@test.com', 'thirty9', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, 'M', 39, NULL, NULL, DATE_SUB(NOW(), INTERVAL 27 MINUTE), NOW()),
    (10, 'user-social-id-10', 'fifty@test.com', 'fifty', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, NULL, 50, NULL, NULL, DATE_SUB(NOW(), INTERVAL 26 MINUTE), NOW()),
    (11, 'user-social-id-11', 'sixty@test.com', 'sixty', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, NULL, 60, NULL, NULL, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW()),
    (12, 'user-social-id-12', 'single9@test.com', 'single9', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, 'M', 9, NULL, NULL, DATE_SUB(NOW(), INTERVAL 24 MINUTE), NOW());
