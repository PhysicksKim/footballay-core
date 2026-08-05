LOCK TABLE fixture_core, fixture_api_sports, league_apisports_season, league_season_core IN SHARE ROW EXCLUSIVE MODE;

DO $$
DECLARE
    target_count BIGINT;
    unresolved_count BIGINT;
    ambiguous_count BIGINT;
    conflicting_existing_binding_count BIGINT;
    updated_count BIGINT;
    candidate_fixture_ids BIGINT[];
    remaining_null_count BIGINT;
    updated_league_mismatch_count BIGINT;
BEGIN
    SELECT COUNT(DISTINCT fixture.id)
    INTO target_count
    FROM fixture_core fixture
    JOIN fixture_api_sports provider_fixture
        ON provider_fixture.fixture_core_id = fixture.id
    WHERE fixture.league_season_id IS NULL;

    SELECT COUNT(DISTINCT fixture.id)
    INTO unresolved_count
    FROM fixture_core fixture
    JOIN fixture_api_sports provider_fixture
        ON provider_fixture.fixture_core_id = fixture.id
    LEFT JOIN league_apisports_season provider_season
        ON provider_season.id = provider_fixture.season_id
    LEFT JOIN league_season_core core_season
        ON core_season.id = provider_season.league_season_core_id
    WHERE fixture.league_season_id IS NULL
      AND (
          provider_fixture.season_id IS NULL
          OR provider_season.id IS NULL
          OR provider_season.league_season_core_id IS NULL
          OR core_season.id IS NULL
          OR core_season.league_core_id IS DISTINCT FROM fixture.league_id
      );

    IF unresolved_count > 0 THEN
        RAISE EXCEPTION
            'Cannot backfill fixture_core.league_season_id: % fixture(s) have no valid provider/core-season binding',
            unresolved_count;
    END IF;

    SELECT COUNT(*)
    INTO ambiguous_count
    FROM (
        SELECT fixture.id
        FROM fixture_core fixture
        JOIN fixture_api_sports provider_fixture
            ON provider_fixture.fixture_core_id = fixture.id
        JOIN league_apisports_season provider_season
            ON provider_season.id = provider_fixture.season_id
        JOIN league_season_core core_season
            ON core_season.id = provider_season.league_season_core_id
        WHERE fixture.league_season_id IS NULL
          AND core_season.league_core_id = fixture.league_id
        GROUP BY fixture.id
        HAVING COUNT(DISTINCT provider_season.league_season_core_id) > 1
    ) ambiguous_fixtures;

    IF ambiguous_count > 0 THEN
        RAISE EXCEPTION
            'Cannot backfill fixture_core.league_season_id: % fixture(s) have ambiguous core-season candidates',
            ambiguous_count;
    END IF;

    SELECT COUNT(DISTINCT fixture.id)
    INTO conflicting_existing_binding_count
    FROM fixture_core fixture
    JOIN fixture_api_sports provider_fixture
        ON provider_fixture.fixture_core_id = fixture.id
    JOIN league_apisports_season provider_season
        ON provider_season.id = provider_fixture.season_id
    JOIN league_season_core core_season
        ON core_season.id = provider_season.league_season_core_id
    WHERE fixture.league_season_id IS NOT NULL
      AND fixture.league_season_id IS DISTINCT FROM provider_season.league_season_core_id;

    IF conflicting_existing_binding_count > 0 THEN
        RAISE EXCEPTION
            'Cannot backfill fixture_core.league_season_id: % fixture(s) have conflicting existing/provider season bindings',
            conflicting_existing_binding_count;
    END IF;

    WITH valid_bindings AS (
        SELECT
            fixture.id AS fixture_core_id,
            provider_season.league_season_core_id
        FROM fixture_core fixture
        JOIN fixture_api_sports provider_fixture
            ON provider_fixture.fixture_core_id = fixture.id
        JOIN league_apisports_season provider_season
            ON provider_season.id = provider_fixture.season_id
        JOIN league_season_core core_season
            ON core_season.id = provider_season.league_season_core_id
        WHERE fixture.league_season_id IS NULL
          AND core_season.league_core_id = fixture.league_id
    ),
    candidates AS (
        SELECT
            fixture_core_id,
            MIN(league_season_core_id) AS league_season_core_id
        FROM valid_bindings
        GROUP BY fixture_core_id
        HAVING COUNT(DISTINCT league_season_core_id) = 1
    )
    SELECT COALESCE(ARRAY_AGG(fixture_core_id), ARRAY[]::BIGINT[])
    INTO candidate_fixture_ids
    FROM candidates;

    WITH valid_bindings AS (
        SELECT
            fixture.id AS fixture_core_id,
            provider_season.league_season_core_id
        FROM fixture_core fixture
        JOIN fixture_api_sports provider_fixture
            ON provider_fixture.fixture_core_id = fixture.id
        JOIN league_apisports_season provider_season
            ON provider_season.id = provider_fixture.season_id
        JOIN league_season_core core_season
            ON core_season.id = provider_season.league_season_core_id
        WHERE fixture.league_season_id IS NULL
          AND core_season.league_core_id = fixture.league_id
    ),
    candidates AS (
        SELECT
            fixture_core_id,
            MIN(league_season_core_id) AS league_season_core_id
        FROM valid_bindings
        GROUP BY fixture_core_id
        HAVING COUNT(DISTINCT league_season_core_id) = 1
    )
    UPDATE fixture_core fixture
    SET league_season_id = candidates.league_season_core_id
    FROM candidates
    WHERE fixture.id = candidates.fixture_core_id
      AND fixture.league_season_id IS NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;

    IF updated_count <> target_count THEN
        RAISE EXCEPTION
            'fixture_core.league_season_id backfill count mismatch: expected %, updated %',
            target_count,
            updated_count;
    END IF;

    SELECT COUNT(DISTINCT fixture.id)
    INTO remaining_null_count
    FROM fixture_core fixture
    JOIN fixture_api_sports provider_fixture
        ON provider_fixture.fixture_core_id = fixture.id
    WHERE fixture.league_season_id IS NULL;

    IF remaining_null_count > 0 THEN
        RAISE EXCEPTION
            'fixture_core.league_season_id backfill incomplete: % API Sports fixture(s) remain',
            remaining_null_count;
    END IF;

    SELECT COUNT(*)
    INTO updated_league_mismatch_count
    FROM fixture_core fixture
    JOIN league_season_core core_season
        ON core_season.id = fixture.league_season_id
    WHERE fixture.id = ANY(candidate_fixture_ids)
      AND core_season.league_core_id IS DISTINCT FROM fixture.league_id;

    IF updated_league_mismatch_count > 0 THEN
        RAISE EXCEPTION
            'fixture_core.league_season_id backfill produced % fixture(s) with a league mismatch',
            updated_league_mismatch_count;
    END IF;
END $$;
