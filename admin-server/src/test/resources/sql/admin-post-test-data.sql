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
    (2, 'author-social-id-2', 'author2@test.com', 'slugger2', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW()),
    (3, 'author-social-id-3', 'author3@test.com', 'slugger3', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW()),
    (4, 'normal-user-social-id', 'user4@test.com', 'user4', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW());

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
    (101, 2, 'LG 경기 직관 후기입니다', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (102, 3, 'LG 경기 MVP 이야기', 'LG', 'ACTIVE', 0, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
    (103, 2, 'KIA 경기 분석 글', 'KIA', 'HIDDEN', 2, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (104, 3, '삭제된 게시글입니다', 'ALL', 'DELETED', 0, 0, 0, 0, 0, NOW(), DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
    (105, 2, '신고된 게시글입니다', 'ALL', 'REPORTED', 0, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW());
