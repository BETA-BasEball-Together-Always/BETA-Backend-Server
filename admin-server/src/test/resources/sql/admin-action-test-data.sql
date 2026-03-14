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
    (2, 'target-user-social-id', 'target-user@test.com', 'target-user', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW()),
    (3, 'hidden-post-user-social-id', 'post-user@test.com', 'post-user', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', NOW(), NOW()),
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
    (101, 3, '정상 게시글', 'ALL', 'ACTIVE', 1, 0, 0, 0, 0, NULL, NOW(), NOW()),
    (102, 3, '숨김 게시글', 'ALL', 'HIDDEN', 0, 0, 0, 0, 0, NULL, NOW(), NOW()),
    (103, 3, '삭제된 게시글', 'ALL', 'DELETED', 0, 0, 0, 0, 0, NOW(), NOW(), NOW());

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
    (201, 4, NULL, 101, '정상 댓글', 0, 0, 'ACTIVE', NULL, NOW(), NOW());
