-- Post Detail + Comments API 테스트용 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(3, 'blocked123', 'KAKAO', '차단유저', 'blocked@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 1, NOW(), NOW()),
(2, '두산', 1, NOW(), NOW());

-- 게시글 (댓글 25개로 페이징 테스트)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 1, '두산 베어스 오늘 경기 최고였어요!', 'DOOSAN', 'ACTIVE', 25, 5, 1, 2, 3, NOW(), NOW());

-- 댓글 없는 게시글
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(101, 1, '댓글 없는 게시글', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- 게시글 이미지
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 100, 1, 'https://storage.example.com/images/post100_1.jpg', 'game1.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(2, 100, 1, 'https://storage.example.com/images/post100_2.jpg', 'game2.jpg', 'uuid2.jpg', 1, 204800, 'image/jpeg', 'ACTIVE', NOW(), NOW());

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 100, 1, NOW(), NOW()),
(2, 100, 2, NOW(), NOW());

-- 부모 댓글 25개 (depth=0) - 페이징 테스트용
-- 첫 페이지: id 1~20 (삭제 댓글 id=5는 첫 페이지에 포함)
-- 다음 페이지: id 21~25
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(1, 100, 2, NULL, '댓글 1', 0, 5, 'ACTIVE', '2025-01-01 00:00:01', NOW()),
(2, 100, 2, NULL, '댓글 2', 0, 0, 'ACTIVE', '2025-01-01 00:00:02', NOW()),
(3, 100, 2, NULL, '댓글 3', 0, 0, 'ACTIVE', '2025-01-01 00:00:03', NOW()),
(4, 100, 2, NULL, '댓글 4', 0, 0, 'ACTIVE', '2025-01-01 00:00:04', NOW()),
(5, 100, 2, NULL, '삭제된 댓글 (답글 있음)', 0, 0, 'DELETED', '2025-01-01 00:00:05', NOW()),
(6, 100, 2, NULL, '댓글 6', 0, 0, 'ACTIVE', '2025-01-01 00:00:06', NOW()),
(7, 100, 2, NULL, '댓글 7', 0, 0, 'ACTIVE', '2025-01-01 00:00:07', NOW()),
(8, 100, 2, NULL, '댓글 8', 0, 0, 'ACTIVE', '2025-01-01 00:00:08', NOW()),
(9, 100, 2, NULL, '댓글 9', 0, 0, 'ACTIVE', '2025-01-01 00:00:09', NOW()),
(10, 100, 3, NULL, '차단유저 댓글', 0, 0, 'ACTIVE', '2025-01-01 00:00:10', NOW()),
(11, 100, 2, NULL, '댓글 11', 0, 0, 'ACTIVE', '2025-01-01 00:00:11', NOW()),
(12, 100, 2, NULL, '댓글 12', 0, 0, 'ACTIVE', '2025-01-01 00:00:12', NOW()),
(13, 100, 2, NULL, '댓글 13', 0, 0, 'ACTIVE', '2025-01-01 00:00:13', NOW()),
(14, 100, 2, NULL, '댓글 14', 0, 0, 'ACTIVE', '2025-01-01 00:00:14', NOW()),
(15, 100, 2, NULL, '댓글 15', 0, 0, 'ACTIVE', '2025-01-01 00:00:15', NOW()),
(16, 100, 2, NULL, '댓글 16', 0, 0, 'ACTIVE', '2025-01-01 00:00:16', NOW()),
(17, 100, 2, NULL, '댓글 17', 0, 0, 'ACTIVE', '2025-01-01 00:00:17', NOW()),
(18, 100, 2, NULL, '댓글 18', 0, 0, 'ACTIVE', '2025-01-01 00:00:18', NOW()),
(19, 100, 2, NULL, '댓글 19', 0, 0, 'ACTIVE', '2025-01-01 00:00:19', NOW()),
(20, 100, 2, NULL, '댓글 20', 0, 0, 'ACTIVE', '2025-01-01 00:00:20', NOW()),
(21, 100, 2, NULL, '댓글 21', 0, 0, 'ACTIVE', '2025-01-01 00:00:21', NOW()),
(22, 100, 2, NULL, '댓글 22', 0, 0, 'ACTIVE', '2025-01-01 00:00:22', NOW()),
(23, 100, 1, NULL, '댓글 23 (본인)', 0, 3, 'ACTIVE', '2025-01-01 00:00:23', NOW()),
(24, 100, 2, NULL, '댓글 24', 0, 0, 'ACTIVE', '2025-01-01 00:00:24', NOW()),
(25, 100, 2, NULL, '댓글 25', 0, 0, 'ACTIVE', '2025-01-01 00:00:25', NOW());

-- 대댓글 (depth=1)
-- 댓글 1에 대한 대댓글 2개
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(101, 100, 1, 1, '댓글 1에 대한 답글 1', 1, 0, 'ACTIVE', '2025-01-01 00:01:01', NOW()),
(102, 100, 2, 1, '댓글 1에 대한 답글 2', 1, 2, 'ACTIVE', '2025-01-01 00:01:02', NOW());

-- 삭제된 댓글(5)에 대한 활성 대댓글 (삭제된 댓글입니다로 표시되어야 함)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(103, 100, 2, 5, '삭제된 댓글의 답글', 1, 0, 'ACTIVE', '2025-01-01 00:01:03', NOW());

-- 차단유저 댓글(10)에 대한 대댓글 (부모가 제외되어야 함)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(104, 100, 2, 10, '차단유저 댓글의 답글', 1, 0, 'ACTIVE', '2025-01-01 00:01:04', NOW());

-- 댓글 좋아요 (user 1이 댓글 1, 102에 좋아요)
INSERT INTO comment_like (id, user_id, comment_id, created_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW()),
(2, 1, 102, NOW(), NOW());

-- 사용자 차단 (user 1이 user 3을 차단)
INSERT INTO user_block (id, blocker_id, blocked_id, created_at, updated_at) VALUES
(1, 1, 3, NOW(), NOW());
