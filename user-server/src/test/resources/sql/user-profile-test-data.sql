-- User Profile API 테스트용 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구'),
('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
('KIA', 'KIA 타이거즈', 'KIA Tigers', '광주-기아 챔피언스 필드', '광주광역시');

-- 사용자
-- user 1: DOOSAN 팀 (조회자)
-- user 2: DOOSAN 팀 (같은 팀 대상)
-- user 3: LG 팀 (다른 팀 대상)
-- user 4: DOOSAN 팀 (차단당한 사용자)
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, bio, created_at, updated_at) VALUES
(1, 'viewer123', 'KAKAO', '조회자', 'viewer@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '두산 팬입니다', NOW(), NOW()),
(2, 'same_team123', 'KAKAO', '같은팀유저', 'sameteam@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '저도 두산 팬!', NOW(), NOW()),
(3, 'other_team123', 'KAKAO', '다른팀유저', 'otherteam@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'LG', 'LG 트윈스 팬', NOW(), NOW()),
(4, 'blocked123', 'KAKAO', '차단된유저', 'blocked@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '차단당한 유저', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 5, NOW(), NOW()),
(2, '두산', 3, NOW(), NOW()),
(3, 'LG', 2, NOW(), NOW());

-- user 2 (같은 팀)의 게시글 - ALL 채널 3개, DOOSAN 채널 2개
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 2, '같은팀 ALL 게시글 1', 'ALL', 'ACTIVE', 0, 5, 0, 0, 0, NOW(), NOW()),
(101, 2, '같은팀 ALL 게시글 2', 'ALL', 'ACTIVE', 1, 3, 0, 0, 0, NOW(), NOW()),
(102, 2, '같은팀 ALL 게시글 3', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(103, 2, '같은팀 DOOSAN 게시글 1', 'DOOSAN', 'ACTIVE', 2, 10, 0, 0, 0, NOW(), NOW()),
(104, 2, '같은팀 DOOSAN 게시글 2', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- user 3 (다른 팀)의 게시글 - ALL 채널 2개, LG 채널 3개
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(200, 3, '다른팀 ALL 게시글 1', 'ALL', 'ACTIVE', 0, 2, 0, 0, 0, NOW(), NOW()),
(201, 3, '다른팀 ALL 게시글 2', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(202, 3, '다른팀 LG 게시글 1', 'LG', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(203, 3, '다른팀 LG 게시글 2', 'LG', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(204, 3, '다른팀 LG 게시글 3', 'LG', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- user 4 (차단된 사용자)의 게시글
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(300, 4, '차단된유저 게시글', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- 삭제된 게시글 (조회 안 됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, deleted_at, created_at, updated_at) VALUES
(400, 2, '삭제된 게시글', 'ALL', 'DELETED', 0, 0, 0, 0, 0, NOW(), NOW(), NOW());

-- user 1이 user 4를 차단
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 1, 4, NOW(), NOW());

-- 게시글 이미지 (게시글 103에만)
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 103, 2, 'https://storage.example.com/images/post103_1.jpg', 'game1.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW());

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 103, 1, NOW(), NOW()),
(2, 103, 2, NOW(), NOW());

-- 페이징 테스트를 위한 추가 게시글 (user 2, ALL 채널 12개 더)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(500, 2, '같은팀 ALL 페이징 1', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(501, 2, '같은팀 ALL 페이징 2', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(502, 2, '같은팀 ALL 페이징 3', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(503, 2, '같은팀 ALL 페이징 4', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(504, 2, '같은팀 ALL 페이징 5', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(505, 2, '같은팀 ALL 페이징 6', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(506, 2, '같은팀 ALL 페이징 7', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(507, 2, '같은팀 ALL 페이징 8', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(508, 2, '같은팀 ALL 페이징 9', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(509, 2, '같은팀 ALL 페이징 10', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(510, 2, '같은팀 ALL 페이징 11', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(511, 2, '같은팀 ALL 페이징 12', 'ALL', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());
