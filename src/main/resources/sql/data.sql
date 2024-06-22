-- users 테이블에 admin 사용자 추가
INSERT INTO users (username, password, enabled)
VALUES ('gyechunhoe', '{noop}admin', true);
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{noop}admin', true);

-- authorities 테이블에 admin 사용자의 ROLE_ADMIN 권한 추가
INSERT INTO authorities (user_id, authority)
VALUES ((SELECT id FROM users WHERE username = 'gyechunhoe'), 'ROLE_ADMIN');
