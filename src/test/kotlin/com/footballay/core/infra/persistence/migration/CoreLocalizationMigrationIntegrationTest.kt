package com.footballay.core.infra.persistence.migration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

/** 실제 PostgreSQL에서 V11 Core localization schema를 검증합니다. */
@Testcontainers
@DisplayName("Core localization Flyway migration 통합 테스트")
class CoreLocalizationMigrationIntegrationTest {
    @Test
    fun `V11 creates localization tables and drops deprecated columns`() {
        inMigratedSchema { connection ->
            assertThat(connection.columnExists("player_core", "name_ko")).isFalse()
            assertThat(connection.columnExists("team_core", "name_ko")).isFalse()
            assertThat(connection.columnExists("league_core", "name_ko")).isFalse()

            localizationTables.forEach { table ->
                assertThat(connection.tableExists(table)).isTrue()
            }

            val coreIds = connection.insertCores()
            connection.execute(
                "INSERT INTO player_core_localization (core_id, locale, name, short_name) VALUES (${coreIds.player}, 'en', NULL, NULL)",
            )
            connection.execute(
                "INSERT INTO team_core_localization (core_id, locale, name, short_name) VALUES (${coreIds.team}, 'ko', '', ' ')",
            )
            connection.execute(
                "INSERT INTO league_core_localization (core_id, locale, name, short_name) VALUES (${coreIds.league}, 'en', 'League EN', NULL)",
            )

            assertThat(connection.queryLong("SELECT COUNT(*) FROM player_core_localization")).isEqualTo(1)
            assertThat(connection.queryLong("SELECT COUNT(*) FROM team_core_localization")).isEqualTo(1)
            assertThat(connection.queryLong("SELECT COUNT(*) FROM league_core_localization")).isEqualTo(1)
        }
    }

    @Test
    fun `V11 enforces unique locale and cascades core deletion`() {
        inMigratedSchema { connection ->
            val coreIds = connection.insertCores()

            localizationTables.zip(coreIds.asList()).forEach { (table, coreId) ->
                connection.execute("INSERT INTO $table (core_id, locale) VALUES ($coreId, 'en')")
                assertThatThrownBy {
                    connection.execute("INSERT INTO $table (core_id, locale) VALUES ($coreId, 'en')")
                }.hasMessageContaining("duplicate key value violates unique constraint")
            }

            connection.execute("DELETE FROM player_core WHERE id = ${coreIds.player}")
            connection.execute("DELETE FROM team_core WHERE id = ${coreIds.team}")
            connection.execute("DELETE FROM league_core WHERE id = ${coreIds.league}")

            localizationTables.forEach { table ->
                assertThat(connection.queryLong("SELECT COUNT(*) FROM $table")).isZero()
            }
        }
    }

    private fun inMigratedSchema(block: (Connection) -> Unit) {
        val schema = "v11_${UUID.randomUUID().toString().replace("-", "")}"
        flyway(schema).migrate()
        connection(schema).use { connection ->
            try {
                block(connection)
            } finally {
                connection.execute("DROP SCHEMA $schema CASCADE")
            }
        }
    }

    private fun flyway(schema: String) =
        Flyway
            .configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .schemas(schema)
            .defaultSchema(schema)
            .createSchemas(true)
            .locations("classpath:db/migration")
            .load()

    private fun connection(schema: String): Connection =
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).also { connection ->
            connection.execute("SET search_path TO $schema")
        }

    private fun Connection.insertCores(): CoreIds =
        CoreIds(
            player = queryLong("INSERT INTO player_core (uid, name, auto_generated) VALUES ('player-${UUID.randomUUID()}', 'Player', false) RETURNING id"),
            team = queryLong("INSERT INTO team_core (uid, name, national, auto_generated) VALUES ('team-${UUID.randomUUID()}', 'Team', false, false) RETURNING id"),
            league = queryLong("INSERT INTO league_core (uid, name, available, auto_generated, match_collect) VALUES ('league-${UUID.randomUUID()}', 'League', false, false, 'NONE') RETURNING id"),
        )

    private fun Connection.tableExists(table: String): Boolean =
        queryLong("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = '$table'") == 1L

    private fun Connection.columnExists(
        table: String,
        column: String,
    ): Boolean =
        queryLong(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = '$table' AND column_name = '$column'",
        ) == 1L

    private fun Connection.queryLong(sql: String): Long =
        createStatement().use { statement ->
            statement.executeQuery(sql).use { resultSet ->
                check(resultSet.next())
                resultSet.getLong(1)
            }
        }

    private fun Connection.execute(sql: String) {
        createStatement().use { statement -> statement.execute(sql) }
    }

    private data class CoreIds(
        val player: Long,
        val team: Long,
        val league: Long,
    ) {
        fun asList(): List<Long> = listOf(player, team, league)
    }

    private companion object {
        val localizationTables =
            listOf(
                "player_core_localization",
                "team_core_localization",
                "league_core_localization",
            )

        @Container
        @JvmStatic
        val postgres =
            PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("footballay_localization_test")
                .withUsername("test")
                .withPassword("test")
    }
}
