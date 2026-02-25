-- Home API 테스트용 더미 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구'),
('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
('SAMSUNG', '삼성 라이온즈', 'Samsung Lions', '대구삼성라이온즈파크', '대구시');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'SAMSUNG', NOW(), NOW()),
(3, 'blocked123', 'KAKAO', '차단유저', 'blocked@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'LG', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 3, NOW(), NOW()),
(2, 'KBO', 2, NOW(), NOW());

-- ALL 채널 게시글들 (인기 게시글 테스트용)
-- 최근 24시간 내 게시글 (인기순: 100 > 101 > 102)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 1, '오늘 경기 대박이었다!', 'ALL', 'ACTIVE', 15, 10, 2, 5, 8, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
(101, 2, '삼성 화이팅!', 'ALL', 'ACTIVE', 5, 5, 1, 3, 2, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
(102, 1, '야구 보러 가자', 'ALL', 'ACTIVE', 2, 3, 0, 1, 1, DATE_SUB(NOW(), INTERVAL 10 HOUR), NOW());

-- 차단된 사용자의 ALL 채널 게시글 (필터링되어야 함)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(103, 3, '차단유저 인기글', 'ALL', 'ACTIVE', 10, 20, 5, 10, 15, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW());

-- 24시간 이전 게시글 (조회되면 안됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(104, 1, '어제 게시글', 'ALL', 'ACTIVE', 20, 50, 10, 20, 30, DATE_SUB(NOW(), INTERVAL 30 HOUR), NOW());

-- 다른 채널 게시글 (조회되면 안됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(105, 1, '두산 채널 글', 'DOOSAN', 'ACTIVE', 5, 30, 5, 10, 15, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW());

-- 삭제된 게시글 (조회되면 안됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, deleted_at, created_at, updated_at) VALUES
(106, 1, '삭제된 ALL 게시글', 'ALL', 'DELETED', 5, 40, 5, 10, 15, NOW(), DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW());

-- 게시글 이미지
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 100, 1, 'https://storage.example.com/images/post100_1.jpg', 'image1.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW());

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 100, 1, NOW(), NOW()),
(2, 100, 2, NOW(), NOW()),
(3, 101, 1, NOW(), NOW());

-- 사용자 차단 (user 1이 user 3을 차단)
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 1, 3, NOW(), NOW());
