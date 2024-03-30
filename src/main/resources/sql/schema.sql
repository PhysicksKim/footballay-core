create table users
(
    id       INT                   NOT NULL AUTO_INCREMENT,
    username varchar_ignorecase(50) not null,
    password varchar_ignorecase(500) not null,
    enabled  boolean                not null,
    PRIMARY KEY (id),
    UNIQUE (username)
);

create table authorities
(
    id        INT                   NOT NULL AUTO_INCREMENT,
    user_id   INT                   NOT NULL,
--     username varchar_ignorecase(50) not null,
    authority varchar_ignorecase(50) not null,
    constraint fk_authorities_users foreign key (user_id) references users (id),
--     constraint fk_username foreign key (username) references users (username),
    PRIMARY KEY (id)
);

create unique index ix_auth_user_authority on authorities (user_id, authority);