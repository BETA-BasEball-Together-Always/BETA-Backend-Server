SET @t1 = NOW(6);
SET @t2 = DATE_ADD(@t1, INTERVAL 1 SECOND);
SET @t3 = DATE_ADD(@t1, INTERVAL 2 SECOND);
SET @t4 = DATE_ADD(@t1, INTERVAL 3 SECOND);

INSERT INTO baseball_teams (code, team_name_kr) VALUES
('DOOSAN', '두산 베어스');

INSERT INTO users (id, nickname, bio, favorite_team_code, status, created_at, updated_at) VALUES
(10, '두산직관러', '오늘도 야구 보러 감', 'DOOSAN', 'ACTIVE', @t1, @t1);

INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(20, '두산', 3, @t2, @t2),
(21, '야구', 5, @t2, @t2);

INSERT INTO posts (id, user_id, channel, content, status, deleted_at, created_at, updated_at) VALUES
(100, 10, 'ALL', '두산 야구 경기 직관 후기입니다. 오늘 타선 집중력이 좋았습니다.', 'ACTIVE', NULL, @t3, @t3);

INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(30, 100, 20, @t4, @t4),
(31, 100, 21, @t4, @t4);
