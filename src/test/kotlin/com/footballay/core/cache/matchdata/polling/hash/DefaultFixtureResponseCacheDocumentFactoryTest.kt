package com.footballay.core.cache.matchdata.polling.hash

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultFixtureResponseCacheDocumentFactoryTest {
    private lateinit var factory: FixtureResponseCacheDocumentFactory
    private lateinit var etagHasher: FixtureEtagHasher

    @BeforeEach
    fun setUp() {
        val objectMapper =
            JsonMapper
                .builder()
                .addModule(KotlinModule.Builder().build())
                .addModule(JavaTimeModule())
                .build()

        etagHasher = DefaultFixtureEtagHasher()
        factory =
            DefaultFixtureResponseCacheDocumentFactory(
                objectMapper = objectMapper,
                canonicalizer = DefaultFixtureResponseCanonicalizer(),
                canonicalJsonWriter = DefaultFixtureCanonicalJsonWriter(objectMapper),
                etagHasher = etagHasher,
            )
    }

    @Test
    fun `create liveStatus - snapshot json 과 weak etag 를 함께 생성한다`() {
        val response =
            FixtureLiveStatusResponse(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusResponse.LiveStatus(
                        elapsed = 77,
                        shortStatus = "2H",
                        longStatus = "Second Half",
                        score = FixtureLiveStatusResponse.Score(home = 1, away = 2),
                    ),
            )

        val document = factory.create(response)

        assertThat(document.snapshotJson)
            .isEqualTo("""{"fixtureUid":"fixture-1","liveStatus":{"elapsed":77,"shortStatus":"2H","longStatus":"Second Half","score":{"home":1,"away":2}}}""")
        assertThat(document.etagHash).isNotBlank()
    }

    @Test
    fun `create events - snapshot json 은 입력 순서를 유지하고 weak etag 는 canonical 기준으로 동일하다`() {
        val first =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )
        val second =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 2, type = "Card"),
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                    ),
            )

        val firstDocument = factory.create(first)
        val secondDocument = factory.create(second)

        assertThat(firstDocument.snapshotJson).isNotEqualTo(secondDocument.snapshotJson)
        assertThat(firstDocument.snapshotJson.indexOf(""""sequence":3""")).isLessThan(firstDocument.snapshotJson.indexOf(""""sequence":1"""))
        assertThat(firstDocument.snapshotJson.indexOf(""""sequence":1""")).isLessThan(firstDocument.snapshotJson.indexOf(""""sequence":2"""))
        assertThat(firstDocument.etagHash).isEqualTo(secondDocument.etagHash)
    }

    @Test
    fun `create events - semantic 값이 달라지면 weak etag 도 달라진다`() {
        val first =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )
        val second =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Subst"),
                    ),
            )

        val firstDocument = factory.create(first)
        val secondDocument = factory.create(second)

        assertThat(firstDocument.etagHash).isNotEqualTo(secondDocument.etagHash)
    }

    private fun event(
        sequence: Int,
        type: String,
    ) = FixtureEventsResponse.EventInfo(
        sequence = sequence,
        elapsed = sequence * 10,
        extraTime = null,
        team = FixtureEventsResponse.TeamInfo(teamUid = "team-$sequence", name = "Team $sequence", koreanName = null, playerColor = null),
        player = FixtureEventsResponse.PlayerInfo(matchPlayerUid = "mp-$sequence", playerUid = "player-$sequence", name = "Player $sequence", koreanName = null, number = sequence),
        assist = null,
        type = type,
        detail = "$type detail",
        comments = null,
    )
}
