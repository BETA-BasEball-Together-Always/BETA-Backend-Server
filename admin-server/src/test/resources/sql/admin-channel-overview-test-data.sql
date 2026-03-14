INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
    ('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구'),
    ('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
    ('KIWOOM', '키움 히어로즈', 'Kiwoom Heroes', '고척스카이돔', '서울시 구로구'),
    ('KT', 'KT 위즈', 'KT Wiz', '수원KT위즈파크', '경기도 수원시'),
    ('SSG', 'SSG 랜더스', 'SSG Landers', '인천SSG랜더스필드', '인천시 미추홀구'),
    ('KIA', 'KIA 타이거즈', 'KIA Tigers', '광주기아챔피언스필드', '광주시 북구'),
    ('SAMSUNG', '삼성 라이온즈', 'Samsung Lions', '대구삼성라이온즈파크', '대구시 수성구'),
    ('NC', 'NC 다이노스', 'NC Dinos', '창원NC파크', '경남 창원시'),
    ('HANWHA', '한화 이글스', 'Hanwha Eagles', '대전한화생명볼파크', '대전시 중구'),
    ('LOTTE', '롯데 자이언츠', 'Lotte Giants', '사직야구장', '부산시 동래구');

INSERT INTO users (
    id,
    social_id,
    email,
    nickname,
    social_provider,
    status,
    role,
    signup_step,
    favorite_team_code,
    withdrawn_at,
    created_at,
    updated_at
) VALUES
    (1, 'admin-social-id', 'admin@test.com', 'admin-user', 'KAKAO', 'ACTIVE', 'ADMIN', 'COMPLETED', 'LG', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    (2, 'user-social-id-2', 'user2@test.com', 'slugger2', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'LG', NULL, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
    (3, 'user-social-id-3', 'user3@test.com', 'slugger3', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'KIA', NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
    (4, 'user-social-id-4', 'user4@test.com', 'slugger4', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'DOOSAN', NULL, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
    (5, 'user-social-id-5', 'user5@test.com', 'slugger5', 'KAKAO', 'ACTIVE', 'USER', 'COMPLETED', 'HANWHA', NULL, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW());

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
    (1001, 2, 'LG d-6 post', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL -10 HOUR), NOW()),
    (1002, 2, 'LG d-5 post', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL -10 HOUR), NOW()),
    (1003, 2, 'LG d-4 post', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -10 HOUR), NOW()),
    (1004, 2, 'LG d-2 post', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL -10 HOUR), NOW()),
    (1005, 2, 'LG d-1 post', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL -9 HOUR), NOW()),
    (1006, 2, 'LG today post 1', 'LG', 'ACTIVE', 2, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
    (1007, 2, 'LG today post 2', 'LG', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),

    (2001, 3, 'KIA d-5 post', 'KIA', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL -11 HOUR), NOW()),
    (2002, 3, 'KIA d-4 post', 'KIA', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -11 HOUR), NOW()),
    (2003, 3, 'KIA d-3 post', 'KIA', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL -11 HOUR), NOW()),
    (2004, 3, 'KIA d-2 post', 'KIA', 'ACTIVE', 2, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL -11 HOUR), NOW()),
    (2005, 3, 'KIA d-1 post', 'KIA', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL -11 HOUR), NOW()),
    (2006, 3, 'KIA today post', 'KIA', 'ACTIVE', 2, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),

    (3001, 4, 'DOOSAN d-6 post', 'DOOSAN', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL -9 HOUR), NOW()),
    (3002, 4, 'DOOSAN d-3 post', 'DOOSAN', 'ACTIVE', 0, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL -9 HOUR), NOW()),
    (3003, 4, 'DOOSAN d-1 post', 'DOOSAN', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL -8 HOUR), NOW()),
    (3004, 4, 'DOOSAN today post', 'DOOSAN', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),

    (4001, 5, 'HANWHA d-6 post', 'HANWHA', 'ACTIVE', 0, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL -8 HOUR), NOW()),
    (4002, 5, 'HANWHA d-4 post', 'HANWHA', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -8 HOUR), NOW()),
    (4003, 5, 'HANWHA d-2 post', 'HANWHA', 'ACTIVE', 2, 0, 0, 0, 0, NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL -8 HOUR), NOW()),

    (9001, 2, 'ALL channel post', 'ALL', 'ACTIVE', 1, 0, 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW());

INSERT INTO comment (
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
    (5001, 3, NULL, 1001, 'LG d-6 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL -12 HOUR), NOW()),
    (5002, 3, NULL, 1002, 'LG d-5 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL -12 HOUR), NOW()),
    (5003, 3, NULL, 1003, 'LG d-4 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -12 HOUR), NOW()),
    (5004, 3, NULL, 1003, 'LG d-3 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL -12 HOUR), NOW()),
    (5005, 3, NULL, 1004, 'LG d-2 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL -12 HOUR), NOW()),
    (5006, 3, NULL, 1005, 'LG d-1 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL -12 HOUR), NOW()),
    (5007, 3, NULL, 1006, 'LG today comment 1', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 170 MINUTE), NOW()),
    (5008, 4, NULL, 1006, 'LG today comment 2', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 150 MINUTE), NOW()),
    (5009, 5, NULL, 1005, 'LG today comment 3', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 70 MINUTE), NOW()),

    (5101, 2, NULL, 2001, 'KIA d-4 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -13 HOUR), NOW()),
    (5102, 2, NULL, 2002, 'KIA d-3 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL -13 HOUR), NOW()),
    (5103, 2, NULL, 2003, 'KIA d-2 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL -13 HOUR), NOW()),
    (5104, 2, NULL, 2006, 'KIA today comment 1', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 110 MINUTE), NOW()),
    (5105, 4, NULL, 2006, 'KIA today comment 2', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 100 MINUTE), NOW()),
    (5106, 4, NULL, 2005, 'KIA today comment 3', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 95 MINUTE), NOW()),
    (5107, 5, NULL, 2004, 'KIA today comment 4', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW()),
    (5108, 5, NULL, 2004, 'KIA today comment 5', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 85 MINUTE), NOW()),

    (5201, 2, NULL, 3001, 'DOOSAN d-6 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL -14 HOUR), NOW()),
    (5202, 2, NULL, 3003, 'DOOSAN d-1 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL -14 HOUR), NOW()),
    (5203, 3, NULL, 3004, 'DOOSAN today comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 220 MINUTE), NOW()),

    (5301, 2, NULL, 4002, 'HANWHA d-4 comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL -15 HOUR), NOW()),
    (5302, 2, NULL, 4003, 'HANWHA today comment 1', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 80 MINUTE), NOW()),
    (5303, 3, NULL, 4003, 'HANWHA today comment 2', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 75 MINUTE), NOW()),

    (5901, 2, NULL, 9001, 'ALL today comment', 0, 0, 'ACTIVE', NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW());
