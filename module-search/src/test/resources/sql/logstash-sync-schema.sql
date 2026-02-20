DROP TABLE IF EXISTS post_hashtag;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS hashtag;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS baseball_teams;

CREATE TABLE baseball_teams (
    code VARCHAR(50) PRIMARY KEY,
    team_name_kr VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    nickname VARCHAR(100) NOT NULL,
    bio TEXT,
    favorite_team_code VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE hashtag (
    id BIGINT PRIMARY KEY,
    tag_name VARCHAR(50) NOT NULL,
    usage_count BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE posts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE post_hashtag (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);
