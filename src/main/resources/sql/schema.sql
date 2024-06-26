-- users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COLLATE utf8_general_ci,
    password VARCHAR(500) NOT NULL COLLATE utf8_general_ci,
    enabled BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);

-- authorities 테이블 생성
CREATE TABLE IF NOT EXISTS authorities (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    authority VARCHAR(50) NOT NULL COLLATE utf8_general_ci,
    CONSTRAINT fk_authorities_users FOREIGN KEY (user_id) REFERENCES users (id),
    PRIMARY KEY (id)
);

-- unique 인덱스 생성
CREATE UNIQUE INDEX ix_auth_user_authority ON authorities (user_id, authority);

-- persistent_logins 테이블 생성
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL COLLATE utf8_general_ci,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL COLLATE utf8_general_ci,
    last_used TIMESTAMP NOT NULL
);