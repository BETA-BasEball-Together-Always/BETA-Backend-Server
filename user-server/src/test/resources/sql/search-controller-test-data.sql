-- Search Controller API 테스트 전용 더미 데이터

-- 야구팀
INSERT INTO baseball_teams (code, team_name_kr, team_name_en, home_stadium, stadium_address) VALUES
('DOOSAN', '두산 베어스', 'Doosan Bears', '잠실야구장', '서울시 송파구'),
('LG', 'LG 트윈스', 'LG Twins', '잠실야구장', '서울시 송파구'),
('SAMSUNG', '삼성 라이온즈', 'Samsung Lions', '대구삼성라이온즈파크', '대구광역시');

-- 사용자
INSERT INTO users (id, social_id, social_provider, nickname, email, signup_step, status, role, favorite_team_code, created_at, updated_at) VALUES
(1, 'search-test-user-1', 'KAKAO', '검색테스트유저', 'search1@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '2026-02-20 10:00:00', '2026-02-20 10:00:00'),
(2, 'search-test-user-2', 'KAKAO', '두산응원단장', 'search2@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'DOOSAN', '2026-02-20 10:01:00', '2026-02-20 10:01:00'),
(3, 'search-test-user-3', 'KAKAO', '엘지팬클럽', 'search3@test.com', 'COMPLETED', 'ACTIVE', 'USER', 'LG', '2026-02-20 10:02:00', '2026-02-20 10:02:00');

-- 해시태그
INSERT INTO hashtag (id, tag_name, usage_count, created_at, updated_at) VALUES
(11, '두산', 10, '2026-02-20 10:10:00', '2026-02-20 10:10:00'),
(12, '야구', 8, '2026-02-20 10:10:01', '2026-02-20 10:10:01'),
(13, '엘지', 5, '2026-02-20 10:10:02', '2026-02-20 10:10:02');

-- 게시글 (PostPort 조회/응답 보강용)
INSERT INTO posts (
    id, user_id, content, channel, status,
    comment_count, like_count, sad_count, fun_count, hype_count,
    created_at, updated_at
) VALUES
(200, 2, '오늘 두산 경기 직관 다녀왔는데 초반에는 분위기가 좀 가라앉아 있어서 걱정했어요. 그런데 5회 이후부터 타선이 살아나면서 흐름이 완전히 바뀌었고, 특히 주자가 쌓인 상황에서 집중타가 나온 장면은 아직도 기억에 남아요. 마지막 이닝 수비도 깔끔해서 경기 끝나고 나오는 길에 팬들 표정이 다 밝았습니다.', 'ALL', 'ACTIVE', 3, 7, 0, 1, 2, '2026-02-20 11:00:00', '2026-02-20 11:00:00'),
(201, 3, '엘지 경기 보면서 느낀 건 수비 조직력이 확실히 좋아졌다는 점이에요. 어려운 타구를 무리해서 처리하려고 하기보다 기본 플레이를 안정적으로 가져가서 실수가 적었고, 투수 교체 타이밍도 꽤 합리적이었습니다. 공격에서는 장타보다 연결이 잘 돼서 답답하지 않았고, 전체적으로 팀이 차분하게 운영된 느낌이었어요.', 'ALL', 'ACTIVE', 1, 2, 0, 0, 1, '2026-02-20 11:01:00', '2026-02-20 11:01:00'),
(202, 2, '두산 타선이 요즘처럼 찬스에서 침착하게 점수를 내주면 팀 경기력이 훨씬 안정적으로 보입니다. 오늘도 무리한 스윙보다는 카운트를 유리하게 가져가면서 공을 골라내는 모습이 좋았고, 하위 타순까지 출루가 이어지니까 상위 타순 부담도 줄어든 것 같아요. 이런 흐름이면 다음 시리즈도 충분히 기대해볼 만합니다.', 'DOOSAN', 'ACTIVE', 2, 5, 1, 0, 1, '2026-02-20 11:02:00', '2026-02-20 11:02:00');

-- 게시글 이미지
INSERT INTO post_image (
    id, post_id, user_id, img_url, origin_name, new_name, sort, file_size, mime_type, status, created_at, updated_at
) VALUES
(21, 200, 2, 'https://storage.example.com/images/search-post-200.jpg', 'search-200.jpg', 'uuid-search-200.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', '2026-02-20 11:05:00', '2026-02-20 11:05:00'),
(22, 202, 2, 'https://storage.example.com/images/search-post-202.jpg', 'search-202.jpg', 'uuid-search-202.jpg', 0, 102400, 'image/jpeg', 'ACTIVE', '2026-02-20 11:06:00', '2026-02-20 11:06:00');

-- 게시글-해시태그 연결
INSERT INTO post_hashtag (id, post_id, hashtag_id, created_at, updated_at) VALUES
(31, 200, 11, '2026-02-20 11:10:00', '2026-02-20 11:10:00'),
(32, 200, 12, '2026-02-20 11:10:01', '2026-02-20 11:10:01'),
(33, 201, 13, '2026-02-20 11:10:02', '2026-02-20 11:10:02'),
(34, 202, 11, '2026-02-20 11:10:03', '2026-02-20 11:10:03');
