package com.footballay.core.web.admin.apisports.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.facade.AvailableFixtureFacade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AdminFixtureAvailableWebServiceTest {
    @Mock
    private lateinit var availableFixtureFacade: AvailableFixtureFacade

    private val fixtureApiId = 100L
    private val fixtureUid = "fixture-1"

    @Test
    fun `available true 요청은 facade addAvailableFixture로 라우팅한다`() {
        val service = service()
        whenever(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .thenReturn(DomainResult.Success(fixtureUid))

        val result = service.setFixtureAvailable(fixtureApiId, true)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.uid).isEqualTo(fixtureUid)
        assertThat(response.available).isTrue()
        verify(availableFixtureFacade).addAvailableFixture(fixtureApiId)
        verify(availableFixtureFacade, never()).removeAvailableFixture(fixtureApiId)
    }

    @Test
    fun `available false 요청은 facade removeAvailableFixture로 라우팅한다`() {
        val service = service()
        whenever(availableFixtureFacade.removeAvailableFixture(fixtureApiId))
            .thenReturn(DomainResult.Success(fixtureUid))

        val result = service.setFixtureAvailable(fixtureApiId, false)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.uid).isEqualTo(fixtureUid)
        assertThat(response.available).isFalse()
        verify(availableFixtureFacade).removeAvailableFixture(fixtureApiId)
        verify(availableFixtureFacade, never()).addAvailableFixture(fixtureApiId)
    }

    @Test
    fun `facade 실패 결과는 그대로 반환한다`() {
        val service = service()
        val fail = DomainFail.NotFound("FIXTURE_API_SPORTS", fixtureApiId.toString())
        whenever(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .thenReturn(DomainResult.Fail(fail))

        val result = service.setFixtureAvailable(fixtureApiId, true)

        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        assertThat((result as DomainResult.Fail).error).isEqualTo(fail)
    }

    private fun service(): AdminFixtureAvailableWebService =
        AdminFixtureAvailableWebService(
            availableFixtureFacade = availableFixtureFacade,
        )
}
