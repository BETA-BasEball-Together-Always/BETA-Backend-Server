-- User Management API 테스트용 데이터 (Bio 수정, 회원탈퇴)

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, bio, created_at, updated_at) VALUES
(1, 'test123', 'KAKAO', '테스트유저', 'test@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '기존 한줄소개', NOW(), NOW()),
(2, 'other123', 'KAKAO', '다른유저', 'other@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', NULL, NOW(), NOW()),
(3, 'withdrawn123', 'KAKAO', '탈퇴유저', 'withdrawn@test.com', 'COMPLETED', 'WITHDRAWN', 'USER', 'DOOSAN', NULL, NOW(), NOW());
