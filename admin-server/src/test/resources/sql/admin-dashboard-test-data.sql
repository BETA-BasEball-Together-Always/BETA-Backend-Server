INSERT INTO users (
    id,
    social_id,
    email,
    nickname,
    social_provider,
    status,
    role,
    signup_step,
    withdrawn_at,
    created_at,
    updated_at
) VALUES
    (1, 'admin-social-id', 'admin@test.com', 'admin-user', 'KAKAO', 'ACTIVE', 'ADMIN', 'COMPLETED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
    (2, 'user-social-id-2', 'user2@test.com', 'slugger2', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
    (3, 'user-social-id-3', 'user3@test.com', 'slugger3', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (4, 'user-social-id-4', 'user4@test.com', 'withdrawn4', 'KAKAO', 'WITHDRAWN', 'USER', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    (5, 'user-social-id-5', 'user5@test.com', 'yesterday5', 'KAKAO', 'SUSPENDED', 'USER', 'COMPLETED', NULL, DATE_SUB(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 30 MINUTE), NOW());

INSERT INTO posts (
    id,
    user_id,
    content,
    channel,
    status,
    comment_count,
    like_count,
    sad_count,
    fun_count,
    hype_count,
    deleted_at,
    created_at,
    updated_at
) VALUES
    (101, 2, '오늘 첫 번째 피드', 'ALL', 'ACTIVE', 2, 7, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (102, 3, '오늘 두 번째 피드', 'ALL', 'ACTIVE', 1, 4, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
    (103, 1, '오늘 세 번째 피드', 'ALL', 'ACTIVE', 0, 3, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (201, 2, '어제 동일 시각 포함 피드 1', 'ALL', 'ACTIVE', 5, 10, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 1 HOUR), NOW()),
    (202, 3, '어제 동일 시각 포함 피드 2', 'ALL', 'ACTIVE', 3, 8, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 2 HOUR), NOW()),
    (50, 1, '오래된 피드', 'ALL', 'ACTIVE', 0, 1, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW());

INSERT INTO hashtag (
    id,
    tag_name,
    usage_count,
    created_at,
    updated_at
) VALUES
    (31, '오늘의경기', 234, NOW(), NOW()),
    (32, '직관', 189, NOW(), NOW()),
    (33, '응원가', 156, NOW(), NOW()),
    (34, '기타', 10, NOW(), NOW());

INSERT INTO post_image (
    id,
    post_id,
    user_id,
    img_url,
    origin_name,
    new_name,
    sort,
    file_size,
    mime_type,
    status,
    created_at,
    updated_at
) VALUES
    (1001, 101, 2, 'https://cdn.beta.test/post101-thumb.jpg', 'p101-0.jpg', 'p101-0.jpg', 0, 1024, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
    (1002, 101, 2, 'https://cdn.beta.test/post101-sub.jpg', 'p101-1.jpg', 'p101-1.jpg', 1, 2048, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
    (1003, 103, 1, 'https://cdn.beta.test/post103-thumb.jpg', 'p103-0.jpg', 'p103-0.jpg', 0, 2048, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
    (1004, 201, 2, 'https://cdn.beta.test/post201-thumb.jpg', 'p201-0.jpg', 'p201-0.jpg', 0, 2048, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
    (1005, 202, 3, 'https://cdn.beta.test/post202-deleted.jpg', 'p202-0.jpg', 'p202-0.jpg', 0, 2048, 'image/jpeg', 'DELETED', NOW(), NOW());
