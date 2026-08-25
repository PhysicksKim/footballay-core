package com.footballay.core.web.admin.localization.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.AiLocalizationApplyChange
import com.footballay.core.localization.AiLocalizationApplyResult
import com.footballay.core.localization.AiLocalizationUpdate
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.ai.AiLocalizationEntityType
import com.footballay.core.web.admin.localization.ai.AiLocalizationExportContextLoader
import com.footballay.core.web.admin.localization.ai.AiLocalizationExportValidator
import com.footballay.core.web.admin.localization.ai.AiLocalizationImportValidator
import com.footballay.core.web.admin.localization.ai.AiLocalizationImportValidationResult
import com.footballay.core.web.admin.localization.ai.PlayerExportContext
import com.footballay.core.web.admin.localization.ai.TeamExportContext
import com.footballay.core.web.admin.localization.ai.ValidatedAiLocalizationImport
import com.footballay.core.web.admin.localization.ai.ValidatedAiLocalizationImportItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationFailureResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/** AI localization export/import orchestration 계약을 확인합니다. */
@ExtendWith(MockitoExtension::class)
class AdminAiLocalizationWebServiceTest {
    private val objectMapper = ObjectMapper()

    @Mock
    private lateinit var localizationFacade: LocalizationFacade

    @Mock
    private lateinit var exportValidator: AiLocalizationExportValidator

    @Mock
    private lateinit var exportContextLoader: AiLocalizationExportContextLoader

    @Mock
    private lateinit var importValidator: AiLocalizationImportValidator

    @Test
    @DisplayName("Team export 응답에 요청 locale별 localization과 빈 값을 조립한다")
    fun exportForAi_assemblesTeamExportResponse() {
        val request = AiLocalizationExportRequest(AiLocalizationEntityType.TEAM, "league-1", locales = listOf("en", "ko"), uids = listOf("team-1"))
        whenever(exportValidator.validate(request)).thenReturn(DomainResult.Success(listOf(SupportedLocale.EN, SupportedLocale.KO)))
        whenever(exportContextLoader.loadTeams("league-1", listOf("team-1"))).thenReturn(
            DomainResult.Success(TeamExportContext(LeagueModel("league-1", "Premier League", null, true), listOf(TeamModel("team-1", "Arsenal", "ARS")))),
        )
        whenever(localizationFacade.findTeamLocalizations(listOf("team-1"), listOf(SupportedLocale.EN, SupportedLocale.KO))).thenReturn(
            listOf(CoreLocalizationModel("team-1", SupportedLocale.KO, "아스널", "ARS", false)),
        )

        val response = (service().exportForAi(request) as DomainResult.Success).value

        assertThat(response.context.league.originalName).isEqualTo("Premier League")
        assertThat(response.context.team).isNull()
        assertThat(response.items.single().localizations["en"]?.name).isNull()
        assertThat(response.items.single().localizations["ko"]?.name).isEqualTo("아스널")
    }

    @Test
    @DisplayName("Player export 응답에 Team 문맥과 빈 localization을 조립한다")
    fun exportForAi_assemblesPlayerExportResponse() {
        val request = AiLocalizationExportRequest(AiLocalizationEntityType.PLAYER, "league-1", "team-1", listOf("en"), listOf("player-1"))
        whenever(exportValidator.validate(request)).thenReturn(DomainResult.Success(listOf(SupportedLocale.EN)))
        whenever(exportContextLoader.loadPlayers("league-1", "team-1", listOf("player-1"))).thenReturn(
            DomainResult.Success(
                PlayerExportContext(
                    LeagueModel("league-1", "Premier League", null, true),
                    TeamModel("team-1", "Arsenal", "ARS"),
                    listOf(PlayerModel("player-1", "Bukayo Saka", null, null, null)),
                ),
            ),
        )
        whenever(localizationFacade.findPlayerLocalizations(listOf("player-1"), listOf(SupportedLocale.EN))).thenReturn(emptyList())
        val response = (service().exportForAi(request) as DomainResult.Success).value

        assertThat(response.context.team?.uid).isEqualTo("team-1")
        assertThat(response.items.single().localizations["en"]?.name).isNull()
    }

    @Test
    @DisplayName("Player export는 teamUid 없이는 validation 실패를 반환한다")
    fun exportForAi_returnsValidationWhenPlayerTeamUidIsMissing() {
        val request = AiLocalizationExportRequest(AiLocalizationEntityType.PLAYER, "league-1", locales = listOf("en"), uids = listOf("player-1"))
        whenever(exportValidator.validate(request)).thenReturn(DomainResult.Success(listOf(SupportedLocale.EN)))

        val result = service().exportForAi(request)

        assertThat(result).isEqualTo(
            DomainResult.Fail(
                DomainFail.Validation.single("TEAM_UID_REQUIRED", "teamUid는 PLAYER export에 필요합니다."),
            ),
        )
    }

    @Test
    @DisplayName("검증된 import를 entityType별 bulk apply로 전달하고 응답으로 변환한다")
    fun applyAiImport_dispatchesValidatedItemsAndMapsResult() {
        val teamImport = ValidatedAiLocalizationImport(AiLocalizationEntityType.TEAM, listOf(ValidatedAiLocalizationImportItem(0, "team-1", SupportedLocale.KO, "아스널", null)))
        val playerImport = ValidatedAiLocalizationImport(AiLocalizationEntityType.PLAYER, listOf(ValidatedAiLocalizationImportItem(0, "player-1", SupportedLocale.EN, null, "Saka")))
        whenever(localizationFacade.applyAiTeamLocalizations(any())).thenReturn(
            AiLocalizationApplyResult(
                1,
                0,
                listOf(
                    AiLocalizationApplyChange(
                        "team-1",
                        SupportedLocale.KO,
                        CoreLocalizationModel("team-1", SupportedLocale.KO, "Arsenal", null, false),
                        CoreLocalizationModel("team-1", SupportedLocale.KO, "아스널", null, true),
                    ),
                ),
            ),
        )
        whenever(localizationFacade.applyAiPlayerLocalizations(any())).thenReturn(AiLocalizationApplyResult(0, 1, emptyList()))

        val teamResponse = service().applyAiImport(teamImport)
        val playerResponse = service().applyAiImport(playerImport)

        assertThat(teamResponse.updatedCount).isEqualTo(1)
        assertThat(teamResponse.changes.single().after.aiGenerated).isTrue()
        assertThat(playerResponse.unchangedCount).isEqualTo(1)
        verify(localizationFacade).applyAiTeamLocalizations(eq(listOf(AiLocalizationUpdate("team-1", SupportedLocale.KO, "아스널", null))))
        verify(localizationFacade).applyAiPlayerLocalizations(eq(listOf(AiLocalizationUpdate("player-1", SupportedLocale.EN, null, "Saka"))))
    }

    @Test
    @DisplayName("import 검증은 apply를 호출하지 않는다")
    fun validateAiImport_doesNotApplyLocalizations() {
        val payload = objectMapper.readTree("{}")
        val validation = AiLocalizationImportValidationResult.failure(AiLocalizationImportValidationFailureResponse(emptyList()))
        whenever(importValidator.validate(payload)).thenReturn(validation)

        assertThat(service().validateAiImport(payload)).isSameAs(validation)
        verifyNoInteractions(localizationFacade)
    }

    private fun service() =
        AdminAiLocalizationWebService(localizationFacade, exportValidator, exportContextLoader, importValidator)
}
