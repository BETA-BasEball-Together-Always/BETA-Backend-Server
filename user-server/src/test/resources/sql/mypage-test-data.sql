-- MyPage API 테스트용 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NOW(), NOW());

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(1, '야구', 3, NOW(), NOW()),
(2, '두산', 2, NOW(), NOW());

-- 내 게시글 (user 1이 작성) - 12개로 페이징 테스트
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(100, 1, '내 게시글 1', 'DOOSAN', 'ACTIVE', 2, 3, 0, 0, 0, NOW(), NOW()),
(101, 1, '내 게시글 2', 'DOOSAN', 'ACTIVE', 0, 1, 0, 0, 0, NOW(), NOW()),
(102, 1, '내 게시글 3', 'ALL', 'ACTIVE', 1, 0, 0, 0, 0, NOW(), NOW()),
(103, 1, '내 게시글 4', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(104, 1, '내 게시글 5', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(105, 1, '내 게시글 6', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(106, 1, '내 게시글 7', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(107, 1, '내 게시글 8', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(108, 1, '내 게시글 9', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(109, 1, '내 게시글 10', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(110, 1, '내 게시글 11', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW()),
(111, 1, '내 게시글 12', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NOW(), NOW());

-- 다른 사용자 게시글 (user 2가 작성) - 내가 댓글/좋아요 테스트용
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, created_at, updated_at) VALUES
(200, 2, '다른유저 게시글 1', 'DOOSAN', 'ACTIVE', 1, 1, 0, 0, 0, NOW(), NOW()),
(201, 2, '다른유저 게시글 2', 'ALL', 'ACTIVE', 1, 0, 1, 0, 0, NOW(), NOW()),
(202, 2, '다른유저 게시글 3', 'DOOSAN', 'ACTIVE', 2, 0, 0, 1, 0, NOW(), NOW());

-- 삭제된 게시글 (조회 안 됨)
INSERT INTO posts (id, user_id, content, channel, status, comment_count, like_count, sad_count, fun_count, hype_count, deleted_at, created_at, updated_at) VALUES
(300, 1, '삭제된 내 게시글', 'DOOSAN', 'DELETED', 0, 0, 0, 0, 0, NOW(), NOW(), NOW());

-- 게시글 이미지 (내 게시글 100에만)
INSERT INTO post_image (id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at) VALUES
(1, 100, 1, 'https://storage.example.com/images/post100_1.jpg', 'game1.jpg', 'uuid1.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', NOW(), NOW());

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(1, 100, 1, NOW(), NOW()),
(2, 100, 2, NOW(), NOW());

-- 내가 작성한 댓글 (user 1이 다른 사용자 게시글에 댓글)
INSERT INTO comment (id, post_id, user_id, parent_id, content, depth, like_count, status, created_at, updated_at) VALUES
(1, 200, 1, NULL, '좋은 글이네요', 0, 0, 'ACTIVE', NOW(), NOW()),
(2, 201, 1, NULL, '공감합니다', 0, 0, 'ACTIVE', NOW(), NOW()),
(3, 202, 1, NULL, '재밌어요', 0, 0, 'ACTIVE', NOW(), NOW()),
(4, 202, 1, NULL, '추가 댓글', 0, 0, 'ACTIVE', NOW(), NOW());

-- 내가 누른 감정표현 (user 1이 다른 게시글에 감정표현)
INSERT INTO emotion (id, post_id, user_id, emotion_type, created_at, updated_at) VALUES
(1, 100, 1, 'LIKE', NOW(), NOW()),
(2, 200, 1, 'LIKE', NOW(), NOW()),
(3, 201, 1, 'SAD', NOW(), NOW()),
(4, 202, 1, 'FUN', NOW(), NOW());
