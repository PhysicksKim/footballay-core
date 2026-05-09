package com.footballay.core.web.admin.apisports.service

import com.footballay.core.admin.apisports.query.AdminApiSportsQueryFacade
import com.footballay.core.common.logging.logger
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto
import com.footballay.core.web.admin.apisports.mapper.FixtureWebMapper
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId

/**
 * 관리자용 Fixture 조회 서비스 구현체
 *
 * Admin web 요청을 admin apisports query facade로 위임하고, web 응답 DTO로 변환합니다.
 */
@Service
class AdminFixtureQueryWebServiceImpl(
    private val adminApiSportsQueryFacade: AdminApiSportsQueryFacade,
) : AdminFixtureQueryWebService {
    val log = logger()

    /**
     * 리그의 Fixture 요약 정보를 조회합니다
     *
     * mode 파라미터에 따라 동작이 달라집니다
     * - exact: at 파라미터의 날짜에 해당하는 Fixture들을 조회합니다
     * - nearest: at 파라미터 기준 가장 가까운 날짜의 Fixture들을 조회합니다
     *
     * **nearest 모드 설명**
     * - at 날짜에 Fixture가 존재하면 해당 날짜의 Fixture들을 반환합니다
     * - at 날짜에 Fixture가 존재하지 않으면 이후 날짜 중 가장 가까운 날짜의 Fixture들을 반환합니다
     * - 이후 날짜에 Fixture가 존재하지 않으면 빈 리스트를 반환합니다
     *
     * @param leagueApiId ApiSports 리그 ID
     * @param at 기준 시각. null일 경우 서버 현재 시각 사용. 날짜 단위로 처리됨
     * @param mode "exact" | "nearest"
     * @return FixtureSummaryDto 리스트
     */
    @PreAuthorize("hasRole('ADMIN')")
    override fun findFixturesByLeague(
        leagueApiId: Long,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
    ): List<FixtureSummaryDto> {
        log.info("findFixturesByLeague called with leagueApiId={}, at={}, mode={}, zoneId={}", leagueApiId, at, mode, zoneId)
        return adminApiSportsQueryFacade
            .findFixturesByLeague(leagueApiId, at, mode, zoneId)
            .map { FixtureWebMapper.toSummaryDto(it) }
    }
}
