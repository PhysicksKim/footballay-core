package com.footballay.core.infra.apisports.syncer.match

import com.footballay.core.infra.apisports.live.ActionAfterMatchSync
import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.live.MatchEntitySave
import com.footballay.core.infra.apisports.syncer.match.base.MatchBaseSync
import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.event.MatchEventSync
import com.footballay.core.infra.apisports.syncer.match.lineup.MatchLineupSync
import com.footballay.core.infra.apisports.syncer.match.loader.MatchDataLoader
import com.footballay.core.infra.apisports.syncer.match.playerstat.MatchPlayerStatSync
import com.footballay.core.infra.apisports.syncer.match.teamstat.MatchTeamStatSync
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Live 경기에서 발생하는 데이터를 동기화합니다.
 *
 * 핵심 책임:
 * - 각 Match 관련 Syncer들을 조율
 * - MatchPlayerContext를 통한 데이터 공유
 * - 동기화 순서 관리 및 오류 처리
 */
@Service
class MatchApiSportsSyncerImpl(
    private val matchDataLoader: MatchDataLoader,
    private val baseMatchSync: MatchBaseSync,
    private val lineupSync: MatchLineupSync,
    private val eventSync: MatchEventSync,
    private val teamStatSync: MatchTeamStatSync,
    private val playerStatSync: MatchPlayerStatSync,
    private val matchEntitySave: MatchEntitySave
) : MatchApiSportsSyncer {

    private val log = logger()

    @Transactional
    override fun syncFixtureMatchEntities(dto: FullMatchSyncDto): ActionAfterMatchSync {
        val fixtureApiId = dto.fixture.id
        log.info("Starting sync match data for fixtureApiId=$fixtureApiId")

        try {
            val context = MatchPlayerContext()

            val baseDto = baseMatchSync.syncBaseMatch(dto)
            val lineupDto = lineupSync.syncLineup(dto, context)
            val eventDto = eventSync.syncEvents(dto, context)
            val teamStatDto = teamStatSync.syncTeamStats(dto)
            val playerStatDto = playerStatSync.syncPlayerStats(dto, context)

            // MatchEntitySave는 모든 동기화가 완료된 후에 호출하여 영속성 보장
            TODO("앞서 추출한 dto 를 바탕으로 Entity 저장 로직 구현해야함")
            val entityBundle = MatchEntityBundle.createEmpty()
            matchDataLoader.loadContext(fixtureApiId, context, entityBundle)
            matchEntitySave.saveAllMatchEntities(entityBundle, context)

            return ActionAfterMatchSync.ongoing(
                dto.fixture.date
            )
        } catch (e: Exception) {
            log.error("Failed to sync full match data for fixture: $fixtureApiId", e)
            throw e
        }
    }
}