-- users 테이블에 admin 사용자 추가
INSERT INTO users (username, password, enabled)
VALUES ('gyechunhoe', '{noop}admin', true) ON DUPLICATE KEY UPDATE username=username;
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{noop}admin', true) ON DUPLICATE KEY UPDATE username=username;

-- authorities 테이블에 admin 사용자의 ROLE_ADMIN 권한 추가
INSERT INTO authorities (user_id, authority)
VALUES ((SELECT id FROM users WHERE username = 'gyechunhoe'), 'ROLE_ADMIN')
    ON DUPLICATE KEY UPDATE user_id=user_id, authority=authority;
INSERT INTO authorities (user_id, authority)
VALUES ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN')
    ON DUPLICATE KEY UPDATE user_id=user_id, authority=authority;