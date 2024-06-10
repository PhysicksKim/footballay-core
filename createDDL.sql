
    create table anonymous_users (
        auto_remote_group_id bigint not null,
        created_date datetime(6),
        last_connected_at datetime(6) not null,
        modified_date datetime(6),
        id binary(16) not null,
        primary key (id)
    ) engine=InnoDB;

    create table api_cache (
        id bigint not null auto_increment,
        last_cached_at datetime(6) not null,
        parameters_json varchar(255) not null,
        api_cache_type enum ('LEAGUE','CURRENT_LEAGUES','CURRENT_LEAGUES_OF_TEAM','LEAGUE_TEAMS','TEAM','SQUAD','PLAYER','FIXTURE','LIVE_FIXTURE','FIXTURES_OF_LEAGUE') not null,
        primary key (id)
    ) engine=InnoDB;

    create table auto_remote_group (
        created_date datetime(6),
        expired_at datetime(6) not null,
        id bigint not null auto_increment,
        last_active_at datetime(6) not null,
        modified_date datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table default_matches (
        id bigint not null auto_increment,
        name varchar(255),
        streamer_hash varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table default_teams (
        id bigint not null auto_increment,
        streamer_hash varchar(255),
        category enum ('epl2324','nation','etc'),
        code enum ('ntg','nwc','lut','liv','mci','mun','bur','bou','brh','bre','shu','ars','ava','eve','wlv','whu','che','cry','tot','ful','kr','bh','jo','my','sa','au','jp','ir','cup','ety'),
        side enum ('A','B'),
        uniform enum ('home','away','third'),
        primary key (id)
    ) engine=InnoDB;

    create table favorite_leagues (
        id bigint not null auto_increment,
        league_id bigint not null,
        korean_name varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table fixtures (
        elapsed integer,
        away_team_id bigint not null,
        date datetime(6),
        fixture_id bigint not null,
        home_team_id bigint not null,
        timestamp bigint,
        long_status varchar(255),
        referee varchar(255),
        short_status varchar(255),
        timezone varchar(255),
        primary key (fixture_id)
    ) engine=InnoDB;

    create table leagues (
        current_season integer,
        league_id bigint not null,
        korean_name varchar(255),
        logo varchar(255),
        name varchar(255) not null,
        primary key (league_id)
    ) engine=InnoDB;

    create table league_team (
        league_id bigint not null,
        team_id bigint not null,
        primary key (league_id, team_id)
    ) engine=InnoDB;

    create table players (
        id bigint not null,
        team_id bigint,
        korean_name varchar(255),
        name varchar(255) not null,
        photo_url varchar(255),
        position varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table streamers (
        created_at datetime(6) not null,
        id bigint not null auto_increment,
        hash varchar(255) not null,
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table team (
        id bigint not null,
        korean_name varchar(255),
        logo varchar(255),
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    alter table streamers 
       add constraint UK_2tc1oipxxftle76uv3dnv75m9 unique (hash);

    alter table streamers 
       add constraint UK_7elwhy8bk9na5h5i0uad4u603 unique (name);

    alter table anonymous_users 
       add constraint FKd1pbs3c4fwwnu70dtokek601q 
       foreign key (auto_remote_group_id) 
       references auto_remote_group (id);

    alter table default_matches 
       add constraint FKicj9gieue0lax2w6nvk5efhvp 
       foreign key (streamer_hash) 
       references streamers (hash);

    alter table default_teams 
       add constraint FKdoui02eu28qs1r4rbws8ttsi5 
       foreign key (streamer_hash) 
       references streamers (hash);

    alter table fixtures 
       add constraint FK15vy4bs60qfie9t5m8hygudso 
       foreign key (away_team_id) 
       references team (id);

    alter table fixtures 
       add constraint FKcotycjc2r44y6mf09ad58w739 
       foreign key (home_team_id) 
       references team (id);

    alter table league_team 
       add constraint FKgu14bepa0c1h0ko743dpbh17r 
       foreign key (league_id) 
       references leagues (league_id);

    alter table league_team 
       add constraint FKrd24h3bourg4odyux7n0p2e2w 
       foreign key (team_id) 
       references team (id);

    alter table players 
       add constraint FKtj6qbvs84bdv8oquqgob0o44u 
       foreign key (team_id) 
       references team (id);
