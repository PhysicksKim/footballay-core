package com.footballay.core.web.admin.localization.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.LocalizationUpsertResult
import com.footballay.core.localization.SupportedLocale
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AdminLocalizationWebServiceTest {
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

    private fun service() = AdminLocalizationWebService(leagueFacade, localizationFacade)
}
