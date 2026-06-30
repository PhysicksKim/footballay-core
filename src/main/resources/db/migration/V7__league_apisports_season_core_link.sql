ALTER TABLE league_apisports_season
    ADD COLUMN league_season_core_id BIGINT;

ALTER TABLE league_apisports_season
    ADD CONSTRAINT fk_league_apisports_season_on_league_season_core
        FOREIGN KEY (league_season_core_id) REFERENCES league_season_core (id);

CREATE INDEX idx_league_apisports_season_core
    ON league_apisports_season (league_season_core_id);
