package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.core.LeagueCoreSyncService
import com.footballay.core.infra.core.dto.LeagueCoreCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * ApiSports 리그 데이터를 Core 시스템과 동기화하는 핵심 구현체
 *
 * ## 주요 책임
 * - ApiSports API에서 받은 리그 데이터를 Core 시스템과 연동
 * - LeagueApiSports 엔티티와 LeagueCore 엔티티 간의 연관관계 관리
 * - 리그 시즌 정보 및 커버리지 데이터 동기화
 * - Core가 없는 리그 데이터에 대한 자동 Core 생성
 *
 * ## 주요 진입 메서드
 * - `saveLeagues()`: 리그 목록을 배치로 동기화 (Core 생성 포함)
 *
 * ## 핵심 동작 패턴
 * 1. **기존 데이터 분석**: LeagueApiSports와 LeagueCore 존재 여부에 따른 케이스 분리
 * 2. **Core 자동 생성**: Core가 없는 ApiSports 데이터에 대해 LeagueCore 자동 생성
 * 3. **시즌 정보 처리**: 리그별 시즌 데이터 및 커버리지 정보 동기화
 * 4. **배치 처리**: 성능 최적화를 위한 배치 저장
 *
 * ## 처리 케이스
 * - **Case 1**: LeagueApiSports + LeagueCore 모두 존재 → 업데이트
 * - **Case 2**: LeagueApiSports만 존재, LeagueCore 없음 → Core 생성 후 연결
 * - **Case 3**: 둘 다 없음 → 새로 생성
 *
 * ## 특별 처리
 * - **시즌 데이터**: 리그별 시즌 정보를 포함한 복합 데이터 구조 처리
 * - **커버리지 정보**: 리그의 데이터 커버리지 범위 정보 관리
 * - **자동 Core 생성**: ApiSports 데이터만 있는 경우 자동으로 Core 생성
 *
 * @author Footballay Core Team
 * @since 1.0.0
 */
@Component
class LeagueApiSportsWithCoreSyncer(
    // ApiSports repositories
    private val leagueApiRepository: LeagueApiSportsRepository,
    private val leagueSeasonRepository: LeagueApiSportsSeasonRepository,
    // Core service
    private val leagueCoreSyncService: LeagueCoreSyncService,
) : LeagueApiSportsSyncer {
    @Transactional
    override fun saveLeagues(dtos: List<LeagueApiSportsCreateDto>) {
        val apiIdList: List<Long> = dtos.map { it.apiId }
        val apiIdEntityMap: Map<Long, LeagueApiSports> =
            leagueApiRepository
                .findLeagueApiSportsInApiId(apiIdList)
                .associateBy { it.apiId }

        // Core 있지만 Api 없는 경우는 조회 불가능
        val case1Dtos = mutableListOf<LeagueApiSportsCreateDto>() // 둘 다 있음
        val case2Dtos = mutableListOf<LeagueApiSportsCreateDto>() // ApiSports 는 있지만 Core는 없음
        val case3Dtos = mutableListOf<LeagueApiSportsCreateDto>() // 둘 다 없음

        dtos.forEach { dto ->
            val apiEntity = apiIdEntityMap[dto.apiId]
            when {
                apiEntity != null && apiEntity.leagueCore != null -> case1Dtos.add(dto)
                apiEntity != null && apiEntity.leagueCore == null -> case2Dtos.add(dto)
                apiEntity == null -> case3Dtos.add(dto)
            }
        }

        // case 1. core O / api O 둘 다 있음
        val case1Entities =
            case1Dtos.map { dto ->
                val apiEntity = apiIdEntityMap[dto.apiId]!!
                updateApiEntity(apiEntity, dto)
                apiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, apiEntity))
                apiEntity
            }
        leagueApiRepository.saveAll(case1Entities)

        // case 2. core X / api O ApiSports는 있지만 Core는 없음
        val newLeagueCoresForCase2 =
            case2Dtos.map { dto ->
                val apiEntity = apiIdEntityMap[dto.apiId]!!
                updateApiEntity(apiEntity, dto)
                // Core 저장을 Service를 통해 처리 (영속성 보장)
                val newCore = leagueCoreSyncService.saveLeagueCore(createLeagueCoreDto(dto))
                apiEntity.leagueCore = newCore
                apiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, apiEntity))
                apiEntity
            }
        leagueApiRepository.saveAll(newLeagueCoresForCase2)

        // case 3. core X / api X 둘 다 없음
        val newLeagueApiSportsForCase3 =
            case3Dtos.map { dto ->
                // Core 저장을 Service를 통해 처리 (영속성 보장)
                val newCore = leagueCoreSyncService.saveLeagueCore(createLeagueCoreDto(dto))
                val newApiEntity = createApiEntity(newCore, dto)
                newApiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, newApiEntity))
                newApiEntity
            }
        leagueApiRepository.saveAll(newLeagueApiSportsForCase3)
    }

    private fun createSeasonEntities(
        seasonDtos: List<LeagueApiSportsSeasonCreateDto>,
        apiEntity: LeagueApiSports,
    ): List<LeagueApiSportsSeason> =
        seasonDtos.map { seasonDto ->
            LeagueApiSportsSeason(
                seasonYear = seasonDto.seasonYear,
                seasonStart = seasonDto.seasonStart?.let { LocalDate.parse(it) },
                seasonEnd = seasonDto.seasonEnd?.let { LocalDate.parse(it) },
                coverage =
                    seasonDto.coverage?.let {
                        createLeagueApiSportsCoverage(it)
                    },
                leagueApiSports = apiEntity,
            )
        }

    private fun createLeagueApiSportsCoverage(it: LeagueApiSportsCoverageCreateDto) =
        LeagueApiSportsCoverage(
            fixturesEvents = it.fixturesEvents,
            fixturesLineups = it.fixturesLineups,
            fixturesStatistics = it.fixturesStatistics,
            fixturesPlayers = it.fixturesPlayers,
            standings = it.standings,
            players = it.players,
            topScorers = it.topScorers,
            topAssists = it.topAssists,
            topCards = it.topCards,
            injuries = it.injuries,
            predictions = it.predictions,
            odds = it.odds,
        )

    private fun createApiEntity(
        newCore: LeagueCore,
        dto: LeagueApiSportsCreateDto,
    ) = LeagueApiSports(
        leagueCore = newCore,
        apiId = dto.apiId,
        name = dto.name,
        type = dto.type,
        logo = dto.logo,
        countryName = dto.countryName,
        countryCode = dto.countryCode,
        countryFlag = dto.countryFlag,
        currentSeason = dto.currentSeason,
    )

    private fun createLeagueCoreDto(dto: LeagueApiSportsCreateDto) =
        LeagueCoreCreateDto(
            name = dto.name,
            autoGenerated = true,
            available = false,
        )

    private fun updateApiEntity(
        apiEntity: LeagueApiSports,
        dto: LeagueApiSportsCreateDto,
    ) {
        apiEntity.apply {
            name = dto.name
            type = dto.type
            logo = dto.logo
            countryName = dto.countryName
            countryCode = dto.countryCode
            countryFlag = dto.countryFlag
            currentSeason = dto.currentSeason
        }
    }
}
