DELETE FROM baseball_teams WHERE code IN ('LG', 'KIA');

INSERT INTO baseball_teams (
    code,
    team_name_kr,
    team_name_en,
    home_stadium,
    stadium_address
) VALUES
    ('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
    ('KIA', 'KIA 타이거즈', 'KIA Tigers', '광주-기아 챔피언스 필드', '광주광역시 북구');

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
    created_at,
    updated_at
) VALUES
    (1, 'admin-social-id', 'admin@test.com', 'admin-user', 'KAKAO', 'ACTIVE', 'ADMIN', 'COMPLETED', 'LG', NOW(), NOW()),
    (2, 'author-social-id-2', 'author2@test.com', 'slugger2', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'LG', NOW(), NOW()),
    (3, 'author-social-id-3', 'author3@test.com', 'slugger3', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'KIA', NOW(), NOW()),
    (4, 'normal-user-social-id', 'user4@test.com', 'user4', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'LG', NOW(), NOW());

INSERT INTO hashtag (
    id,
    tag_name,
    usage_count,
    created_at,
    updated_at
) VALUES
    (1, '관리자', 1, NOW(), NOW()),
    (2, '상세', 1, NOW(), NOW());

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
    (200, 2, '관리자 상세 조회용 게시글', 'LG', 'ACTIVE', 4, 11, 2, 3, 4, NULL, '2026-04-01 10:00:00', NOW()),
    (201, 3, '숨김 처리된 게시글', 'KIA', 'HIDDEN', 0, 0, 0, 0, 0, NULL, '2026-04-01 11:00:00', NOW());

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
    (1, 200, 2, 'https://storage.example.com/images/admin-post-200-1.jpg', 'post200_1.jpg', 'uuid-post200-1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
    (2, 200, 2, 'https://storage.example.com/images/admin-post-200-2.jpg', 'post200_2.jpg', 'uuid-post200-2.jpg', 1, 204800, 'image/jpeg', 'ACTIVE', NOW(), NOW());

INSERT INTO post_hashtag (
    id,
    post_id,
    hashtag_id,
    created_at,
    updated_at
) VALUES
    (1, 200, 1, NOW(), NOW()),
    (2, 200, 2, NOW(), NOW());

INSERT INTO comment (
    id,
    post_id,
    user_id,
    parent_id,
    content,
    depth,
    like_count,
    status,
    created_at,
    updated_at
) VALUES
    (201, 200, 2, NULL, '첫 번째 댓글', 0, 3, 'ACTIVE', '2026-04-01 10:01:00', NOW()),
    (202, 200, 2, NULL, '두 번째 댓글', 0, 2, 'ACTIVE', '2026-04-01 10:02:00', NOW()),
    (203, 200, 3, NULL, '관리자 개인 차단과 무관하게 보여야 하는 댓글', 0, 1, 'ACTIVE', '2026-04-01 10:03:00', NOW()),
    (204, 200, 2, NULL, '삭제된 댓글', 0, 0, 'DELETED', '2026-04-01 10:04:00', NOW()),
    (301, 200, 3, 203, '차단 사용자 답글', 1, 0, 'ACTIVE', '2026-04-01 10:05:00', NOW());

INSERT INTO user_block (
    id,
    blocker_id,
    blocked_id,
    created_at,
    updated_at
) VALUES
    (1, 1, 3, NOW(), NOW());
