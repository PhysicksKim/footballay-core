package com.footballay.core.cache.matchdata.polling.hash

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class DefaultFixtureCanonicalJsonWriterTest {
    private lateinit var writer: FixtureCanonicalJsonWriter
    private lateinit var canonicalizer: FixtureResponseCanonicalizer

    @BeforeEach
    fun setUp() {
        writer =
            DefaultFixtureCanonicalJsonWriter(
                JsonMapper
                    .builder()
                    .addModule(KotlinModule.Builder().build())
                    .addModule(JavaTimeModule())
                    .build(),
            )
        canonicalizer = DefaultFixtureResponseCanonicalizer()
    }

    @Test
    fun `writeAsString - fixture dto를 compact canonical json 으로 직렬화한다`() {
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

        val json = writer.writeAsString(response)

        assertThat(json)
            .isEqualTo("""{"fixtureUid":"fixture-1","liveStatus":{"elapsed":77,"longStatus":"Second Half","score":{"away":2,"home":1},"shortStatus":"2H"}}""")
        assertThat(json).doesNotContain("\n")
    }

    @Test
    fun `writeAsString - null 과 map key order 를 고정한다`() {
        val first =
            CanonicalWriterSample(
                meta = linkedMapOf("z" to 1, "a" to 2),
                note = null,
                payload = NestedPayload(second = "beta", first = "alpha"),
            )
        val second =
            CanonicalWriterSample(
                meta = linkedMapOf("a" to 2, "z" to 1),
                note = null,
                payload = NestedPayload(second = "beta", first = "alpha"),
            )

        val firstJson = writer.writeAsString(first)
        val secondJson = writer.writeAsString(second)

        assertThat(firstJson).isEqualTo(secondJson)
        assertThat(firstJson)
            .isEqualTo("""{"meta":{"a":2,"z":1},"note":null,"payload":{"first":"alpha","second":"beta"}}""")
    }

    @Test
    fun `writeAsBytes - 같은 canonical dto 는 같은 bytes 를 생성한다`() {
        val response =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)
        val firstBytes = writer.writeAsBytes(canonical)
        val secondBytes = writer.writeAsBytes(canonicalizer.canonicalize(response))

        assertThat(firstBytes).isEqualTo(secondBytes)
        assertThat(firstBytes).isEqualTo(writer.writeAsString(canonical).toByteArray(StandardCharsets.UTF_8))
    }

    @Test
    fun `writeAsString - 같은 semantic fixture dto 는 canonicalization 이후 동일한 json 이 된다`() {
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

        val firstJson = writer.writeAsString(canonicalizer.canonicalize(first))
        val secondJson = writer.writeAsString(canonicalizer.canonicalize(second))

        assertThat(firstJson).isEqualTo(secondJson)
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

    private data class CanonicalWriterSample(
        val meta: Map<String, Int>,
        val note: String?,
        val payload: NestedPayload,
    )

    private data class NestedPayload(
        val second: String,
        val first: String,
    )
}
