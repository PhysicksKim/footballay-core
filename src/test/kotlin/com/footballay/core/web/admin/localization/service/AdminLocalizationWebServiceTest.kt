package com.footballay.core.web.admin.localization.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.AiLocalizationApplyChange
import com.footballay.core.localization.AiLocalizationApplyResult
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.LocalizationUpsertResult
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationImport
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportItem
import com.footballay.core.web.admin.localization.ai.AiLocalizationImportValidator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AdminLocalizationWebServiceTest {
    private val objectMapper = ObjectMapper()

    @Mock
    private lateinit var leagueFacade: LeagueFacade

    @Mock
    private lateinit var localizationFacade: LocalizationFacade

    @Test
    @DisplayName("available League와 localization을 UID로 조립한다")
    fun getAvailableLeagues_assemblesLocalizationByUid() {
        whenever(leagueFacade.getAvailableCoreLeagues()).thenReturn(
            DomainResult.Success(
                listOf(
                    LeagueModel("league-1", "League One", null, true),
                    LeagueModel("league-2", "League Two", null, true),
                ),
            ),
        )
        whenever(localizationFacade.findLeagueLocalizations(listOf("league-1", "league-2"), listOf(SupportedLocale.KO))).thenReturn(
            listOf(CoreLocalizationModel("league-1", SupportedLocale.KO, "리그 하나", null, false)),
        )

        val result = service().getAvailableLeagues(SupportedLocale.KO)

        assertThat((result as DomainResult.Success).value)
            .extracting("uid", "originalName", "localization.name")
            .containsExactly(
                Tuple("league-1", "League One", "리그 하나"),
                Tuple("league-2", "League Two", null),
            )
    }

    @Test
    @DisplayName("lookup은 uid와 apiId가 정확히 하나가 아니면 400용 validation 실패를 반환한다")
    fun lookupLeague_returnsValidationFailureWhenLookupParametersAreInvalid() {
        val result = service().lookupLeague(null, null, SupportedLocale.KO)

        assertThat(result).isEqualTo(
            DomainResult.Fail(
                DomainFail.Validation.single("INVALID_LEAGUE_LOOKUP", "uid 또는 apiId 중 하나만 지정해야 합니다."),
            ),
        )
    }

    @Test
    @DisplayName("PUT은 non-null field만 LocalizationFacade에 전달하고 응답을 조립한다")
    fun updateLeagueLocalization_passesPartialFieldsAndAssemblesResponse() {
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(
            DomainResult.Success(LeagueModel("league-1", "League One", null, true)),
        )
        whenever(localizationFacade.upsertLeagueLocalization("league-1", SupportedLocale.KO, "리그 하나", null)).thenReturn(
            DomainResult.Success(
                LocalizationUpsertResult(CoreLocalizationModel("league-1", SupportedLocale.KO, "리그 하나", "L1", false)),
            ),
        )

        val result = service().updateLeagueLocalization("league-1", SupportedLocale.KO, "리그 하나", null)

        assertThat((result as DomainResult.Success).value.localization?.aiGenerated).isFalse()
        verify(localizationFacade).upsertLeagueLocalization(eq("league-1"), eq(SupportedLocale.KO), eq("리그 하나"), eq(null))
    }

    @Test
    @DisplayName("Team AI export는 League context와 여러 locale의 기존 localization을 조립한다")
    fun exportForAi_exportsTeamsWithContextAndMultipleLocales() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.TEAM, "league-1", locales = listOf("en", "ko"), uids = listOf("team-1"))
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(LeagueModel("league-1", "Premier League", null, true)))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(TeamModel("team-1", "Arsenal", "ARS"))))
        whenever(localizationFacade.findTeamLocalizations(listOf("team-1"), listOf(SupportedLocale.EN, SupportedLocale.KO))).thenReturn(
            listOf(CoreLocalizationModel("team-1", SupportedLocale.KO, "아스널", "ARS", false)),
        )

        val response = (aiService().exportForAi(request) as DomainResult.Success).value

        assertThat(response.context.league.originalName).isEqualTo("Premier League")
        assertThat(response.context.team).isNull()
        assertThat(response.items.single().localizations)
            .containsEntry("en", com.footballay.core.web.admin.localization.dto.AiLocalizationExportValue(null, null))
            .containsEntry("ko", com.footballay.core.web.admin.localization.dto.AiLocalizationExportValue("아스널", "ARS"))
    }

    @Test
    @DisplayName("Player AI export는 League와 Team 관계 및 선택 Player를 검증한다")
    fun exportForAi_exportsPlayersWithLeagueAndTeamContext() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.PLAYER, "league-1", "team-1", listOf("en", "ko"), listOf("player-1"))
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(LeagueModel("league-1", "Premier League", null, true)))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(TeamModel("team-1", "Arsenal", "ARS"))))
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(TeamModel("team-1", "Arsenal", "ARS")))
        whenever(leagueFacade.findPlayersByTeamUid("team-1")).thenReturn(DomainResult.Success(listOf(PlayerModel("player-1", "Bukayo Saka", null, null, null))))
        whenever(localizationFacade.findPlayerLocalizations(listOf("player-1"), listOf(SupportedLocale.EN, SupportedLocale.KO))).thenReturn(
            listOf(CoreLocalizationModel("player-1", SupportedLocale.EN, "Bukayo Saka", "Saka", false)),
        )

        val response = (aiService().exportForAi(request) as DomainResult.Success).value

        assertThat(response.context.team?.uid).isEqualTo("team-1")
        assertThat(response.items.single().localizations["en"]?.shortName).isEqualTo("Saka")
        assertThat(response.items.single().localizations["ko"]?.name).isNull()
    }

    @Test
    @DisplayName("AI export는 빈 locale과 UID를 validation 실패로 반환한다")
    fun exportForAi_returnsValidationForEmptySelections() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.TEAM, "league-1", locales = emptyList(), uids = emptyList())

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error)
            .isEqualTo(
                DomainFail.Validation(
                    listOf(
                        DomainFail.Validation.ValidationError("LOCALES_EMPTY", "locales는 비어 있을 수 없습니다.", "locales"),
                        DomainFail.Validation.ValidationError("UIDS_EMPTY", "uids는 비어 있을 수 없습니다.", "uids"),
                    ),
                ),
            )
    }

    @Test
    @DisplayName("AI export는 중복 locale과 UID를 validation 실패로 반환한다")
    fun exportForAi_returnsValidationForDuplicateSelections() {
        val request = AiLocalizationExportRequest(
            AiLocalizationExportEntityType.TEAM,
            "league-1",
            locales = listOf("ko", "ko"),
            uids = listOf("team-1", "team-1"),
        )

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error).isEqualTo(
            DomainFail.Validation(
                listOf(
                    DomainFail.Validation.ValidationError("LOCALES_DUPLICATED", "locales에 중복 값이 있습니다.", "locales"),
                    DomainFail.Validation.ValidationError("UIDS_DUPLICATED", "uids에 중복 값이 있습니다.", "uids"),
                ),
            ),
        )
    }

    @Test
    @DisplayName("AI export는 지원하지 않는 locale을 validation 실패로 반환한다")
    fun exportForAi_returnsValidationForUnsupportedLocale() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.TEAM, "league-1", locales = listOf("fr"), uids = listOf("team-1"))

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error).isEqualTo(
            DomainFail.Validation.single("UNSUPPORTED_LOCALE", "지원하지 않는 locale입니다: fr", "locales"),
        )
    }

    @Test
    @DisplayName("AI export는 요청 Team이 League 문맥에 없으면 validation 실패를 반환한다")
    fun exportForAi_returnsValidationWhenTeamIsOutsideLeagueContext() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.TEAM, "league-1", locales = listOf("ko"), uids = listOf("team-2"))
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(LeagueModel("league-1", "Premier League", null, true)))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(TeamModel("team-1", "Arsenal", "ARS"))))

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error).isEqualTo(
            DomainFail.Validation.single("TEAM_NOT_IN_LEAGUE", "현재 탐색 문맥에 포함되지 않은 UID입니다: team-2", "uids"),
        )
    }

    @Test
    @DisplayName("AI Player export는 Team이 League 문맥에 없으면 validation 실패를 반환한다")
    fun exportForAi_returnsValidationWhenPlayerTeamIsOutsideLeagueContext() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.PLAYER, "league-1", "team-2", listOf("ko"), listOf("player-1"))
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(LeagueModel("league-1", "Premier League", null, true)))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(TeamModel("team-1", "Arsenal", "ARS"))))

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error).isEqualTo(
            DomainFail.Validation.single("TEAM_NOT_IN_LEAGUE", "현재 탐색 문맥에 포함되지 않은 UID입니다: team-2", "uids"),
        )
    }

    @Test
    @DisplayName("AI Player export는 Player가 Team 문맥에 없으면 validation 실패를 반환한다")
    fun exportForAi_returnsValidationWhenPlayerIsOutsideTeamContext() {
        val request = AiLocalizationExportRequest(AiLocalizationExportEntityType.PLAYER, "league-1", "team-1", listOf("ko"), listOf("player-2"))
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(LeagueModel("league-1", "Premier League", null, true)))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(TeamModel("team-1", "Arsenal", "ARS"))))
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(TeamModel("team-1", "Arsenal", "ARS")))
        whenever(leagueFacade.findPlayersByTeamUid("team-1")).thenReturn(DomainResult.Success(listOf(PlayerModel("player-1", "Bukayo Saka", null, null, null))))

        val result = aiService().exportForAi(request)

        assertThat((result as DomainResult.Fail).error).isEqualTo(
            DomainFail.Validation.single("PLAYER_NOT_IN_TEAM", "현재 탐색 문맥에 포함되지 않은 UID입니다: player-2", "uids"),
        )
    }

    @Test
    @DisplayName("AI import는 유효한 Team과 Player payload의 Core UID를 검증하고 write하지 않는다")
    fun validateAiImport_acceptsValidTeamAndPlayerPayloadsWithoutWriting() {
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(TeamModel("team-1", "Arsenal", "ARS")))
        whenever(leagueFacade.findPlayerByUid("player-1")).thenReturn(DomainResult.Success(PlayerModel("player-1", "Bukayo Saka", null, null, null)))

        val teamResult = aiService().validateAiImport(json("""{"version":1,"entityType":"TEAM","items":[{"uid":"team-1","locale":"ko","name":null}]}"""))
        val playerResult = aiService().validateAiImport(json("""{"version":1,"entityType":"PLAYER","items":[{"uid":"player-1","locale":"en","shortName":"Saka"}]}"""))

        assertThat(teamResult.isSuccess).isTrue()
        assertThat(teamResult.value?.entityType).isEqualTo(com.footballay.core.web.admin.localization.dto.AiLocalizationImportEntityType.TEAM)
        assertThat(teamResult.value?.items).extracting("uid", "locale", "name", "shortName").containsExactly(Tuple("team-1", SupportedLocale.KO, null, null))
        assertThat(playerResult.isSuccess).isTrue()
        assertThat(playerResult.value?.entityType).isEqualTo(com.footballay.core.web.admin.localization.dto.AiLocalizationImportEntityType.PLAYER)
        assertThat(playerResult.value?.items).extracting("uid", "locale", "name", "shortName").containsExactly(Tuple("player-1", SupportedLocale.EN, null, "Saka"))
        verifyNoInteractions(localizationFacade)
    }

    @Test
    @DisplayName("AI import는 payload metadata 오류를 수집한다")
    fun validateAiImport_collectsPayloadValidationErrors() {
        val result = aiService().validateAiImport(json("""{"version":2,"entityType":"LEAGUE","items":[]}"""))

        assertThat(result.failure?.errors?.map { it.code })
            .containsExactly("UNSUPPORTED_VERSION", "UNSUPPORTED_ENTITY_TYPE", "ITEMS_EMPTY")
        verifyNoInteractions(localizationFacade)
    }

    @Test
    @DisplayName("AI import는 item의 uid, locale, duplicate, 길이 오류를 한 번에 수집한다")
    fun validateAiImport_collectsItemValidationErrors() {
        val result = aiService().validateAiImport(
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
        verifyNoInteractions(localizationFacade)
    }

    @Test
    @DisplayName("AI import는 존재하지 않는 Core와 value type 오류를 함께 반환한다")
    fun validateAiImport_collectsCoreAndTypeErrors() {
        whenever(leagueFacade.findTeamByUid("missing-team")).thenReturn(DomainResult.Fail(DomainFail.NotFound("TeamCore", "missing-team")))

        val result = aiService().validateAiImport(
            json("""{"version":1,"entityType":"TEAM","items":[{"uid":"missing-team","locale":"ko","name":123},{"uid":"team-1","locale":123}]}"""),
        )

        assertThat(result.failure?.errors?.map { it.code })
            .contains("INVALID_FIELD_TYPE", "CORE_NOT_FOUND")
        assertThat(result.failure?.errors?.first { it.code == "INVALID_FIELD_TYPE" }?.field).isEqualTo("items[0].name")
        verifyNoInteractions(localizationFacade)
    }

    @Test
    @DisplayName("AI import는 검증 완료 item을 다시 파싱하지 않고 Team bulk apply 결과를 응답으로 조립한다")
    fun applyAiImport_appliesValidatedTeamItems() {
        val import = AiLocalizationImport(AiLocalizationImportEntityType.TEAM, listOf(AiLocalizationImportItem(0, "team-1", SupportedLocale.KO, "아스널", null)))
        whenever(localizationFacade.applyAiTeamLocalizations(org.mockito.kotlin.any())).thenReturn(
            AiLocalizationApplyResult(
                updatedCount = 1,
                unchangedCount = 0,
                changes = listOf(
                    AiLocalizationApplyChange(
                        "team-1",
                        SupportedLocale.KO,
                        CoreLocalizationModel("team-1", SupportedLocale.KO, "Arsenal", null, false),
                        CoreLocalizationModel("team-1", SupportedLocale.KO, "아스널", null, true),
                    ),
                ),
            ),
        )

        val response = aiService().applyAiImport(import)

        assertThat(response.updatedCount).isEqualTo(1)
        assertThat(response.unchangedCount).isZero()
        assertThat(response.changes.single().before?.aiGenerated).isFalse()
        assertThat(response.changes.single().after.aiGenerated).isTrue()
        verify(localizationFacade).applyAiTeamLocalizations(
            eq(listOf(com.footballay.core.localization.AiLocalizationUpdate("team-1", SupportedLocale.KO, "아스널", null))),
        )
    }

    @Test
    @DisplayName("AI import는 Player bulk apply를 선택한다")
    fun applyAiImport_appliesValidatedPlayerItems() {
        val import = AiLocalizationImport(AiLocalizationImportEntityType.PLAYER, listOf(AiLocalizationImportItem(0, "player-1", SupportedLocale.EN, null, "Saka")))
        whenever(localizationFacade.applyAiPlayerLocalizations(org.mockito.kotlin.any())).thenReturn(AiLocalizationApplyResult(0, 1, emptyList()))

        val response = aiService().applyAiImport(import)

        assertThat(response.updatedCount).isZero()
        assertThat(response.unchangedCount).isEqualTo(1)
        verify(localizationFacade).applyAiPlayerLocalizations(
            eq(listOf(com.footballay.core.localization.AiLocalizationUpdate("player-1", SupportedLocale.EN, null, "Saka"))),
        )
    }

    private fun json(value: String) = objectMapper.readTree(value)

    private fun service() = AdminLocalizationWebService(leagueFacade, localizationFacade)

    private fun aiService() = AdminAiLocalizationWebService(leagueFacade, localizationFacade, AiLocalizationImportValidator(leagueFacade))
}
