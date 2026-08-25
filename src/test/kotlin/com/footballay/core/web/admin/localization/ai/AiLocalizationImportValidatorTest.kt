package com.footballay.core.web.admin.localization.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.SupportedLocale
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

/** AI import payload 검증과 내부 전달 모델 생성을 확인합니다. */
@ExtendWith(MockitoExtension::class)
class AiLocalizationImportValidatorTest {
    private val objectMapper = ObjectMapper()

    @Mock
    private lateinit var leagueFacade: LeagueFacade

    @Test
    @DisplayName("유효한 Team과 Player payload를 검증된 import 모델로 변환한다")
    fun validate_convertsValidTeamAndPlayerPayloads() {
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(TeamModel("team-1", "Arsenal", "ARS")))
        whenever(leagueFacade.findPlayerByUid("player-1")).thenReturn(DomainResult.Success(PlayerModel("player-1", "Bukayo Saka", null, null, null)))

        val teamResult = validator().validate(json("""{"version":1,"entityType":"TEAM","items":[{"uid":"team-1","locale":"ko","name":null}]}"""))
        val playerResult = validator().validate(json("""{"version":1,"entityType":"PLAYER","items":[{"uid":"player-1","locale":"en","shortName":"Saka"}]}"""))

        assertThat(teamResult.value).isEqualTo(
            ValidatedAiLocalizationImport(
                AiLocalizationEntityType.TEAM,
                listOf(ValidatedAiLocalizationImportItem(0, "team-1", SupportedLocale.KO, null, null)),
            ),
        )
        assertThat(playerResult.value).isEqualTo(
            ValidatedAiLocalizationImport(
                AiLocalizationEntityType.PLAYER,
                listOf(ValidatedAiLocalizationImportItem(0, "player-1", SupportedLocale.EN, null, "Saka")),
            ),
        )
    }

    @Test
    @DisplayName("version, entityType, 빈 items metadata 오류를 함께 반환한다")
    fun validate_collectsPayloadMetadataErrors() {
        val result = validator().validate(json("""{"version":2,"entityType":"LEAGUE","items":[]}"""))

        assertThat(result.failure?.errors?.map { it.code })
            .containsExactly("UNSUPPORTED_VERSION", "UNSUPPORTED_ENTITY_TYPE", "ITEMS_EMPTY")
    }

    @Test
    @DisplayName("item의 UID, locale, 중복, 길이 오류를 함께 반환한다")
    fun validate_collectsItemValidationErrors() {
        val result =
            validator().validate(
                json(
                    """
                    {"version":1,"entityType":"TEAM","items":[
                      {"uid":" ","locale":"fr","name":"${"a".repeat(256)}","shortName":"${"b".repeat(256)}"},
                      {"uid":"team-1","locale":"ko"},
                      {"uid":"team-1","locale":"ko"}
                    ]}
                    """.trimIndent(),
                ),
            )

        assertThat(result.failure?.errors?.map { it.code })
            .contains("UID_BLANK", "UNSUPPORTED_LOCALE", "NAME_TOO_LONG", "SHORT_NAME_TOO_LONG", "DUPLICATE_UID_LOCALE")
    }

    @Test
    @DisplayName("존재하지 않는 Core와 item field type 오류를 함께 반환한다")
    fun validate_collectsCoreAndItemTypeErrors() {
        whenever(leagueFacade.findTeamByUid("missing-team")).thenReturn(DomainResult.Fail(DomainFail.NotFound("TeamCore", "missing-team")))

        val result = validator().validate(
            json("""{"version":1,"entityType":"TEAM","items":[{"uid":"missing-team","locale":"ko","name":123},{"uid":"team-1","locale":123}]}"""),
        )

        assertThat(result.failure?.errors?.map { it.code }).contains("INVALID_FIELD_TYPE", "CORE_NOT_FOUND")
        assertThat(result.failure?.errors?.first { it.code == "INVALID_FIELD_TYPE" }?.field).isEqualTo("items[0].name")
    }

    @Test
    @DisplayName("object가 아닌 root payload를 즉시 거부한다")
    fun validate_rejectsNonObjectPayload() {
        val result = validator().validate(json("[]"))

        assertThat(result.failure?.errors).extracting("code").containsExactly("INVALID_FIELD_TYPE")
    }

    private fun json(value: String) = objectMapper.readTree(value)

    private fun validator() = AiLocalizationImportValidator(leagueFacade)
}
