INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
    ('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
    ('KIA', 'KIA 타이거즈', 'KIA Tigers', '광주기아챔피언스필드', '광주시 북구');

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
    (1, 'admin-social-id', 'admin@test.com', 'admin-user', 'KAKAO', 'ACTIVE', 'ADMIN', 'COMPLETED', 'LG', 'M', 31, '관리자 계정', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    (2, 'user-social-id-2', 'user2@test.com', 'slugger2', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'LG', 'M', 28, 'LG 팬입니다', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (3, 'user-social-id-3', 'user3@test.com', 'slugger3', 'KAKAO', 'SUSPENDED', 'USER', 'COMPLETED', 'KIA', 'F', 25, '기아 응원 중', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
    (4, 'user-social-id-4', 'user4@test.com', 'withdrawn4', 'KAKAO', 'WITHDRAWN', 'USER', 'COMPLETED', NULL, NULL, 33, '탈퇴 사용자', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
    (5, 'user-social-id-5', 'slugger5@test.com', 'slugger5', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, 'F', 24, '잠실 직관파', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (6, 'user-social-id-6', 'rookie6@test.com', 'rookie6', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'LG', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW());
