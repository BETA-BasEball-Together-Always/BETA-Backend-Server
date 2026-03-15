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
    (4, 'comment-user-social-id', 'comment-user@test.com', 'comment-user', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW());

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
    (101, 2, '기준 게시글 1', 'ALL', 'ACTIVE', 2, 0, 0, 0, 0, NULL, NOW(), NOW()),
    (102, 3, '기준 게시글 2', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, NOW(), NOW());

INSERT INTO `comment` (
    id,
    user_id,
    parent_id,
    post_id,
    content,
    depth,
    like_count,
    status,
    deleted_at,
    created_at,
    updated_at
) VALUES
    (201, 4, NULL, 101, '정상 댓글입니다', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (202, 4, NULL, 101, '숨김 댓글입니다', 0, 0, 'HIDDEN', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
    (203, 3, 201, 101, '정상 대댓글입니다', 1, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (204, 4, NULL, 102, '삭제된 댓글입니다', 0, 0, 'DELETED', NOW(), DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW());
