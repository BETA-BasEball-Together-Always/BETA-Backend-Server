-- Post List API 테스트용 더미 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구'),
('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(3, 'blocked123', 'KAKAO', '차단유저', 'blocked@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 3, NOW(), NOW()),
(2, '두산', 2, NOW(), NOW()),
(3, '응원', 1, NOW(), NOW());

-- 게시글 (user_id는 테스트에서 동적으로 설정해야 함 - 이 SQL은 고정 ID 사용)
-- Post 1: 두산 채널, 이미지 2개, 해시태그 2개, 감정 있음
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 1, '두산 베어스 오늘 경기 최고였어요!', 'DOOSAN', 'ACTIVE', 3, 5, 1, 2, 3, NOW(), NOW());

-- Post 2: 두산 채널, 이미지 없음, 해시태그 1개
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(101, 1, '내일 경기도 기대됩니다', 'DOOSAN', 'ACTIVE', 0, 2, 0, 1, 0, NOW(), NOW());

-- Post 3: ALL 채널
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(102, 2, '야구 시즌 시작!', 'ALL', 'ACTIVE', 1, 10, 0, 5, 8, NOW(), NOW());

-- Post 4: LG 채널 (다른 팀)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(103, 2, 'LG 트윈스 화이팅', 'LG', 'ACTIVE', 0, 3, 0, 0, 2, NOW(), NOW());

-- Post 5: 삭제된 게시글 (조회되면 안됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, deleted_at, created_at, updated_at) VALUES
(104, 1, '삭제된 게시글', 'DOOSAN', 'DELETED', 0, 0, 0, 0, 0, NOW(), NOW(), NOW());

-- Post 6: 차단된 사용자의 게시글
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(105, 3, '차단유저 게시글', 'DOOSAN', 'ACTIVE', 0, 1, 0, 0, 0, NOW(), NOW());

-- 게시글 이미지
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 100, 1, 'https://storage.example.com/images/post100_1.jpg', 'game1.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(2, 100, 1, 'https://storage.example.com/images/post100_2.jpg', 'game2.jpg', 'uuid2.jpg', 1, 204800, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(3, 100, 1, 'https://storage.example.com/images/deleted.jpg', 'deleted.jpg', 'uuid3.jpg', 2, 51200, 'image/jpeg', 'DELETED', NOW(), NOW());

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 100, 1, NOW(), NOW()),
(2, 100, 2, NOW(), NOW()),
(3, 101, 2, NOW(), NOW()),
(4, 102, 1, NOW(), NOW());

-- 댓글 (comment_count 검증용)
INSERT INTO comment (id, post_id, user_id, content, depth, like_count, status, created_at, updated_at) VALUES
(1, 100, 2, '좋은 경기였어요!', 0, 2, 'ACTIVE', NOW(), NOW()),
(2, 100, 1, '감사합니다~', 1, 0, 'ACTIVE', NOW(), NOW()),
(3, 100, 2, '다음에도 응원갈게요', 0, 1, 'ACTIVE', NOW(), NOW()),
(4, 102, 1, '시즌 기대됩니다', 0, 3, 'ACTIVE', NOW(), NOW());

-- 사용자 차단 (user 1이 user 3을 차단)
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 1, 3, NOW(), NOW());
