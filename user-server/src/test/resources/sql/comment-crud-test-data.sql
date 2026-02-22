-- Comment CRUD + Emotion API 테스트용 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(3, 'blocked123', 'KAKAO', '차단유저', 'blocked@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW());

-- 게시글
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 1, '댓글 테스트용 게시글', 'DOOSAN', 'ACTIVE', 3, 1, 1, 0, 0, NOW(), NOW()),
(101, 1, '감정표현 테스트용 게시글', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- 기존 댓글 (수정/삭제/좋아요 테스트용)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(1, 100, 1, NULL, '내가 작성한 댓글', 0, 2, 'ACTIVE', NOW(), NOW()),
(2, 100, 2, NULL, '다른 사용자 댓글', 0, 0, 'ACTIVE', NOW(), NOW()),
(3, 100, 1, 2, '답글 테스트', 1, 0, 'ACTIVE', NOW(), NOW());

-- 기존 댓글 좋아요 (user 2가 댓글 1에 좋아요)
INSERT INTO comment_like (id, user_id, comment_id, created_at, updated_at) VALUES
(1, 2, 1, NOW(), NOW()),
(2, 1, 1, NOW(), NOW());

-- 기존 감정표현 (user 1이 post 100에 LIKE, user 2는 post 100에 SAD)
INSERT INTO emotion (id, post_id, user_id, emotion_type, created_at, updated_at) VALUES
(1, 100, 1, 'LIKE', NOW(), NOW()),
(2, 100, 2, 'SAD', NOW(), NOW());

-- 사용자 차단 (user 1이 user 3을 차단)
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 1, 3, NOW(), NOW());
