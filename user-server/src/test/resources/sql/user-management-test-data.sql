-- User Management API 테스트용 데이터 (Bio 수정, 회원탈퇴, 데이터 삭제)

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, bio, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '기존 한줄소개', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NULL, NOW(), NOW()),
(3, 'withdrawn123', 'KAKAO', '탈퇴유저', 'withdrawn@test.com', 'COMPLETED', 'WITHDRAWN', 'USER', 'DOOSAN', NULL, NOW(), NOW()),
(4, 'cleanup_test', 'KAKAO', '삭제테스트', 'cleanup@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '삭제될 bio', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 1, NOW(), NOW());

-- user 4가 작성한 게시글 (삭제 테스트용)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 4, '삭제될 게시글', 'DOOSAN', 'ACTIVE', 2, 1, 0, 0, 0, NOW(), NOW());

-- user 2가 작성한 게시글 (삭제 안 됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(200, 2, '다른유저 게시글', 'DOOSAN', 'ACTIVE', 1, 0, 0, 0, 0, NOW(), NOW());

-- 게시글 이미지 (user 4의 게시글)
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 100, 4, 'https://storage.example.com/images/post100.jpg', 'test.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW());

-- 게시글-해시태그 연결 (user 4의 게시글)
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 100, 1, NOW(), NOW());

-- user 2가 user 4의 게시글(100)에 댓글 작성 (게시글 삭제 시 함께 삭제되어야 함)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(1, 100, 2, NULL, '다른유저가 쓴 댓글', 0, 1, 'ACTIVE', NOW(), NOW()),
(2, 100, 4, NULL, '본인이 쓴 댓글', 0, 0, 'ACTIVE', NOW(), NOW());

-- user 4가 user 2의 게시글(200)에 댓글 작성 (user 4 삭제 시 삭제되어야 함)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(3, 200, 4, NULL, 'user4가 다른 게시글에 쓴 댓글', 0, 0, 'ACTIVE', NOW(), NOW());

-- 댓글 좋아요: user 4가 댓글 1에 좋아요 (user 4 삭제 시 삭제)
INSERT INTO comment_like (id, user_id, comment_id, created_at, updated_at) VALUES
(1, 4, 1, NOW(), NOW());

-- 댓글 좋아요: user 2가 댓글 1에 좋아요 (게시글 100 삭제 시 함께 삭제)
INSERT INTO comment_like (id, user_id, comment_id, created_at, updated_at) VALUES
(2, 2, 1, NOW(), NOW());

-- 감정표현: user 2가 user 4의 게시글(100)에 좋아요 (게시글 삭제 시 삭제)
INSERT INTO emotion (id, post_id, user_id, emotion_type, created_at, updated_at) VALUES
(1, 100, 2, 'LIKE', NOW(), NOW());

-- 감정표현: user 4가 user 2의 게시글(200)에 좋아요 (user 4 삭제 시 삭제)
INSERT INTO emotion (id, post_id, user_id, emotion_type, created_at, updated_at) VALUES
(2, 200, 4, 'LIKE', NOW(), NOW());

-- 차단: user 4가 user 1 차단 (user 4 삭제 시 삭제)
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 4, 1, NOW(), NOW());
