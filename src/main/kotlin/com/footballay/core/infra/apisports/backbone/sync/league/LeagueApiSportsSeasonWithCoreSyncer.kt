package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueSeasonCoreRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * ApiSports 리그 시즌을 Core 시즌에 맞춰 저장하는 syncer.
 *
 * 이 클래스는 "리그" 자체를 만들거나 연결하지 않는다. 호출자는 이미 저장된
 * [LeagueApiSports] 와 [LeagueCore] 의 id 를 넘겨야 하며, 이 syncer 는 그 리그 쌍 아래의
 * 시즌 row 들만 정리한다.
 *
 * ## 핵심 모델
 *
 * 시즌의 실제 identity 는 DB pk 가 아니라 다음 두 key 이다.
 *
 * - Core season: `(leagueCoreId, seasonYear)`
 * - Provider season: `(leagueApiSportsId, seasonYear)`
 *
 * 같은 리그의 같은 seasonYear 라면 두 row 는 같은 현실 시즌을 뜻한다. 다만
 * `LeagueApiSportsSeason` 은 provider 전용 coverage 같은 값을 담기 위해 Core 와 분리되어 있고,
 * `LeagueApiSportsSeason.leagueSeasonCore` 로 해당 Core season 을 1:1 참조한다.
 *
 * ## 처리 흐름
 *
 * 코드는 크게 아래 순서로 읽으면 된다.
 *
 * 1. 입력 전체를 먼저 검증한다.
 *    중복 리그 입력, 중복 seasonYear, null seasonYear, 파싱 불가능한 날짜 형식은 DB row 를 건드리기 전에 막는다.
 *    날짜는 형식만 검증한다. `seasonYear` 와 `seasonStart`/`seasonEnd` 의 연도 관계는 검증하지 않는다.
 * 2. 입력 id 로 `LeagueApiSports` 와 `LeagueCore` 를 조회하고, 둘이 실제로 연결된 리그 쌍인지 확인한다.
 * 3. 기존 provider/core season 을 각각 identity map 으로 한 번에 읽어온다.
 * 4. `LeagueApiSportsSeasonSyncPlanFactory` 가 Core/Provider season 존재 조합을 plan 으로 해석한다.
 * 5. `LeagueApiSportsSeasonSyncPlanSaver` 가 plan 의 변경분을 core 먼저, provider 나중 순서로 batch save 한다.
 *
 * ## 케이스를 줄이는 방식
 *
 * 전체 경우의 수를 `LeagueCore 존재 여부`, `LeagueApiSports 존재 여부`, provider/core season 존재 여부,
 * provider -> core link 여부까지 펼치면 너무 많다. 이 syncer 는 먼저 리그 쪽 경우의 수를 입력 계약으로 줄인다.
 *
 * - `LeagueApiSports` 와 `LeagueCore` 는 반드시 존재해야 한다.
 * - `LeagueApiSports.leagueCore.id` 는 입력의 `leagueCoreId` 와 같아야 한다.
 * - 이 조건이 깨지면 season 을 만들거나 고치지 않고 예외 처리한다.
 *
 * 리그 검증 이후의 season row 존재 조합과 provider -> core link 보정은 plan factory 가 담당한다.
 * Syncer 는 그 case 로직을 직접 갖지 않고, plan 생성과 저장을 연결하는 orchestration 만 맡는다.
 */
@Component
class LeagueApiSportsSeasonWithCoreSyncer(
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val leagueCoreRepository: LeagueCoreRepository,
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository,
    private val leagueSeasonCoreRepository: LeagueSeasonCoreRepository,
    private val planFactory: LeagueApiSportsSeasonSyncPlanFactory,
    private val planSaver: LeagueApiSportsSeasonSyncPlanSaver,
) {
    @Transactional
    fun syncSeasons(leagueSeasonInputs: List<LeagueSeasonSyncInput>) {
        if (leagueSeasonInputs.isEmpty()) {
            return
        }

        validateSyncInputs(leagueSeasonInputs)
        val leagueReferences = findLeagues(leagueSeasonInputs)
        val existingSeasons = findSeasons(leagueReferences)
        val plan =
            planFactory.createPlan(
                LeagueApiSportsSeasonPlanCommand(
                    inputs = leagueSeasonInputs,
                    leagueApiSportsById = leagueReferences.leagueApiSportsById,
                    leagueCoreById = leagueReferences.leagueCoreById,
                    existingCoreSeasonsByIdentity = existingSeasons.coreSeasonsByIdentity,
                    existingProviderSeasonsByIdentity = existingSeasons.providerSeasonsByIdentity,
                ),
            )

        planSaver.save(plan)
    }

    private fun findLeagues(leagueSeasonInputs: List<LeagueSeasonSyncInput>): ManagedLeagueReferences {
        val leagueApiSportsById =
            leagueApiSportsRepository
                .findAllByIdInWithLeagueCore(leagueSeasonInputs.map { it.leagueApiSportsId }.distinct())
                .associateByUnique { requirePersistedId("LeagueApiSports", it.id) }

        val leagueCoreById =
            leagueCoreRepository
                .findAllById(leagueSeasonInputs.map { it.leagueCoreId }.distinct())
                .associateByUnique { requirePersistedId("LeagueCore", it.id) }

        requireLoadedAllIds(
            entityName = "LeagueApiSports",
            requestedIds = leagueSeasonInputs.map { it.leagueApiSportsId },
            loadedIds = leagueApiSportsById.keys,
        )
        requireLoadedAllIds(
            entityName = "LeagueCore",
            requestedIds = leagueSeasonInputs.map { it.leagueCoreId },
            loadedIds = leagueCoreById.keys,
        )
        requireLeagueApiSportsLinkedToCore(leagueSeasonInputs, leagueApiSportsById)

        return ManagedLeagueReferences(
            leagueApiSportsById = leagueApiSportsById,
            leagueCoreById = leagueCoreById,
        )
    }

    private fun findSeasons(leagueReferences: ManagedLeagueReferences): ExistingSeasonRows {
        val providerSeasonsByIdentity =
            leagueApiSportsSeasonRepository
                .findAllByLeagueApiSportsInWithLeagueSeasonCore(leagueReferences.leagueApiSportsById.values)
                .associateByUnique { season ->
                    ProviderSeasonIdentity(
                        leagueApiSportsId = requirePersistedId("LeagueApiSportsSeason.leagueApiSports", season.leagueApiSports?.id),
                        seasonYear = requireNotNull(season.seasonYear) { "LeagueApiSportsSeason must have seasonYear. id=${season.id}" },
                    )
                }

        val coreSeasonsByIdentity =
            leagueSeasonCoreRepository
                .findAllByLeagueIn(leagueReferences.leagueCoreById.values)
                .associateByUnique { season ->
                    CoreSeasonIdentity(
                        leagueCoreId = requirePersistedId("LeagueSeasonCore.league", season.league.id),
                        seasonYear = season.seasonYear,
                    )
                }

        return ExistingSeasonRows(
            providerSeasonsByIdentity = providerSeasonsByIdentity,
            coreSeasonsByIdentity = coreSeasonsByIdentity,
        )
    }

    private fun validateSyncInputs(leagueSeasonInputs: List<LeagueSeasonSyncInput>) {
        requireUniqueLeagueApiSportsInputs(leagueSeasonInputs)
        leagueSeasonInputs.forEach { input ->
            requireUniqueSeasonYears(input.leagueCreateDto)
            input.leagueCreateDto.seasons.forEach { seasonCreateDto ->
                requireNotNull(seasonCreateDto.seasonYear) {
                    "LeagueApiSports seasonYear is required. leagueApiSportsId=${input.leagueApiSportsId}"
                }
                seasonCreateDto.seasonStart?.let { LocalDate.parse(it) }
                seasonCreateDto.seasonEnd?.let { LocalDate.parse(it) }
            }
        }
    }

    private fun requireUniqueLeagueApiSportsInputs(leagueSeasonInputs: List<LeagueSeasonSyncInput>) {
        val leagueApiSportsIds = leagueSeasonInputs.map { it.leagueApiSportsId }
        require(leagueApiSportsIds.size == leagueApiSportsIds.toSet().size) {
            "LeagueApiSports season sync input contains duplicate leagueApiSportsIds: $leagueApiSportsIds"
        }
    }

    private fun requireUniqueSeasonYears(leagueCreateDto: LeagueApiSportsCreateDto) {
        val seasonYears = leagueCreateDto.seasons.mapNotNull { it.seasonYear }
        require(seasonYears.size == seasonYears.toSet().size) {
            "LeagueApiSports sync input contains duplicate seasonYears. leagueApiId=${leagueCreateDto.apiId}, seasonYears=$seasonYears"
        }
    }

    private fun requireLoadedAllIds(
        entityName: String,
        requestedIds: List<Long>,
        loadedIds: Set<Long>,
    ) {
        val missingIds = requestedIds.toSet() - loadedIds
        require(missingIds.isEmpty()) {
            "$entityName not found for season sync. missingIds=$missingIds"
        }
    }

    private fun requireLeagueApiSportsLinkedToCore(
        leagueSeasonInputs: List<LeagueSeasonSyncInput>,
        leagueApiSportsById: Map<Long, LeagueApiSports>,
    ) {
        leagueSeasonInputs.forEach { input ->
            val leagueApiSports = leagueApiSportsById[input.leagueApiSportsId]
            val linkedCoreId = leagueApiSports?.leagueCore?.id
            require(linkedCoreId == input.leagueCoreId) {
                "LeagueApiSports must be linked to the LeagueCore used for season sync. " +
                    "leagueApiSportsId=${input.leagueApiSportsId}, linkedCoreId=$linkedCoreId, requestedLeagueCoreId=${input.leagueCoreId}"
            }
        }
    }

    private fun requirePersistedId(
        entityName: String,
        id: Long?,
    ): Long = requireNotNull(id) { "$entityName must already be persisted before season sync" }

    private inline fun <T, K> Iterable<T>.associateByUnique(keySelector: (T) -> K): Map<K, T> {
        val result = linkedMapOf<K, T>()
        for (element in this) {
            val key = keySelector(element)
            require(!result.containsKey(key)) { "Duplicate season entity detected for identity=$key" }
            result[key] = element
        }
        return result
    }

    private data class ManagedLeagueReferences(
        val leagueApiSportsById: Map<Long, LeagueApiSports>,
        val leagueCoreById: Map<Long, LeagueCore>,
    )

    private data class ExistingSeasonRows(
        val providerSeasonsByIdentity: Map<ProviderSeasonIdentity, LeagueApiSportsSeason>,
        val coreSeasonsByIdentity: Map<CoreSeasonIdentity, LeagueSeasonCore>,
    )
}
