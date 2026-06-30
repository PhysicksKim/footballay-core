ALTER TABLE fixture_core
    ADD COLUMN league_season_id BIGINT;

ALTER TABLE fixture_core
    ADD CONSTRAINT fk_fixture_core_on_league_season FOREIGN KEY (league_season_id) REFERENCES league_season_core (id);

CREATE INDEX idx_fixture_core_league_season_kickoff
    ON fixture_core (league_season_id, kickoff);

CREATE INDEX idx_fixture_core_league_season_available_kickoff
    ON fixture_core (league_season_id, available, kickoff);
