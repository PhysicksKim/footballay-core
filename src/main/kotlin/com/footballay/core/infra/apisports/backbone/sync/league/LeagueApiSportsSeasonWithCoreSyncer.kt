package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
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
 *    대회에 따라 seasonYear 와 실제 진행 기간은 어긋날 수 있고, provider 의 start/end 값도 시즌 중 바뀔 수 있기 때문이다.
 * 2. 입력 id 로 `LeagueApiSports` 와 `LeagueCore` 를 조회하고, 둘이 실제로 연결된 리그 쌍인지 확인한다.
 * 3. 기존 provider/core season 을 각각 identity map 으로 한 번에 읽어온다.
 * 4. 각 season DTO 에 대해 core season 과 provider season 을 찾거나 만들고, 필드와 provider -> core link 를 보정한다.
 * 5. 변경되었거나 새로 만들어진 season 만 모아 batch save 한다.
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
 * 따라서 이 클래스가 실제로 처리하는 season row 상태는 아래 네 가지로 좁혀서 보면 된다.
 *
 * ```text
 *  Core   | Provider | Provider     | 처리
 *  season |  season  | -> Core link |
 * --------|----------|--------------|---------------------------
 * X       | X        | X            | 둘 다 만들고 연결한다
 * O       | X        | X            | core 를 재사용하고 provider 를 만든다
 * X       | O        | X/잘못됨      | core 를 만들고 provider link 를 보정한다
 * O       | O        | O/X/잘못됨    | 둘 다 재사용하고 provider link 를 보정한다
 * ```
 *
 * 정상 운영 흐름에서는 보통 "둘 다 없음" 또는 "둘 다 있고 정상 연결" 만 자주 나온다.
 * 나머지는 과거 데이터, 중간 마이그레이션, 이전 버그로 생긴 오염 상태를 self-healing 하기 위한 경로다.
 *
 * ## current season 규칙
 *
 * `current` 는 Core season 에만 둔다. Provider DTO 의 `currentSeason` 값이 어떤 year 인지만 보고
 * `LeagueSeasonCore.current = (seasonYear == currentSeason)` 으로 맞춘다. 이때 이번 DTO 목록에 없는
 * 기존 Core season 도 current 해제 대상이므로, season DTO loop 전에 기존 core seasons 의 current flag 를 먼저 정렬한다.
 *
 * ## link 보정 규칙
 *
 * provider season 이 이미 있어도 `leagueSeasonCore` 가 null 이거나 잘못된 season 을 가리킬 수 있다.
 * sync 결과는 항상 provider season 이 같은 `(LeagueCore, seasonYear)` 의 core season 을 가리키도록 보정한다.
 * 잘못 참조하던 core season 은 삭제하지 않는다. 이 syncer 의 책임은 season 매핑 정합성 보정이지
 * orphan cleanup 이 아니다.
 */
@Component
class LeagueApiSportsSeasonWithCoreSyncer(
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val leagueCoreRepository: LeagueCoreRepository,
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository,
    private val leagueSeasonCoreRepository: LeagueSeasonCoreRepository,
) {
    @Transactional
    fun syncSeasons(leagueSeasonInputs: List<LeagueSeasonSyncInput>) {
        if (leagueSeasonInputs.isEmpty()) {
            return
        }

        // Validate before loading/mutating managed season rows. This keeps invalid input from
        // partially changing current flags or season links in the persistence context.
        validateSyncInputs(leagueSeasonInputs)
        val leagueReferences = getManagedLeagueReferences(leagueSeasonInputs)
        val existingSeasons = loadExistingSeasonRows(leagueReferences)
        val savePlan = SeasonSavePlan()

        leagueSeasonInputs.forEach { input ->
            syncSeasonsForLeague(
                input = input,
                leagueReferences = leagueReferences,
                existingSeasons = existingSeasons,
                savePlan = savePlan,
            )
        }

        savePlannedChanges(savePlan)
    }

    private fun getManagedLeagueReferences(leagueSeasonInputs: List<LeagueSeasonSyncInput>): ManagedLeagueReferences {
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

    private fun loadExistingSeasonRows(leagueReferences: ManagedLeagueReferences): ExistingSeasonRows {
        val providerSeasonsByIdentity =
            leagueApiSportsSeasonRepository
                .findAllByLeagueApiSportsInWithLeagueSeasonCore(leagueReferences.leagueApiSportsById.values)
                .associateByUnique { season ->
                    ProviderSeasonIdentity(
                        leagueApiSportsId = requirePersistedId("LeagueApiSportsSeason.leagueApiSports", season.leagueApiSports?.id),
                        seasonYear = requireNotNull(season.seasonYear) { "LeagueApiSportsSeason must have seasonYear. id=${season.id}" },
                    )
                }.toMutableMap()

        val coreSeasonsByIdentity =
            leagueSeasonCoreRepository
                .findAllByLeagueIn(leagueReferences.leagueCoreById.values)
                .associateByUnique { season ->
                    CoreSeasonIdentity(
                        leagueCoreId = requirePersistedId("LeagueSeasonCore.league", season.league.id),
                        seasonYear = season.seasonYear,
                    )
                }.toMutableMap()

        return ExistingSeasonRows(
            providerSeasonsByIdentity = providerSeasonsByIdentity,
            coreSeasonsByIdentity = coreSeasonsByIdentity,
        )
    }

    private fun syncSeasonsForLeague(
        input: LeagueSeasonSyncInput,
        leagueReferences: ManagedLeagueReferences,
        existingSeasons: ExistingSeasonRows,
        savePlan: SeasonSavePlan,
    ) {
        val leagueCore = leagueReferences.requireLeagueCore(input.leagueCoreId)
        val leagueApiSports = leagueReferences.requireLeagueApiSports(input.leagueApiSportsId)

        // Existing core seasons may include years that are no longer present in the provider
        // season list. They still need current=false when provider currentSeason moves away.
        updateCurrentFlagForExistingCoreSeasons(
            leagueCoreId = input.leagueCoreId,
            currentSeasonYear = input.leagueCreateDto.currentSeason,
            coreSeasonsByIdentity = existingSeasons.coreSeasonsByIdentity,
        ).forEach { changedCoreSeason ->
            savePlan.coreSeasonsNeedingSave[changedCoreSeason.identity] = changedCoreSeason.coreSeason
        }

        input.leagueCreateDto.seasons.forEach { seasonCreateDto ->
            syncProviderSeasonWithCoreSeason(
                seasonCreateDto = seasonCreateDto,
                currentSeasonYear = input.leagueCreateDto.currentSeason,
                leagueCore = leagueCore,
                leagueCoreId = input.leagueCoreId,
                leagueApiSports = leagueApiSports,
                leagueApiSportsId = input.leagueApiSportsId,
                existingSeasons = existingSeasons,
                savePlan = savePlan,
            )
        }
    }

    private fun syncProviderSeasonWithCoreSeason(
        seasonCreateDto: LeagueApiSportsSeasonCreateDto,
        currentSeasonYear: Int?,
        leagueCore: LeagueCore,
        leagueCoreId: Long,
        leagueApiSports: LeagueApiSports,
        leagueApiSportsId: Long,
        existingSeasons: ExistingSeasonRows,
        savePlan: SeasonSavePlan,
    ) {
        val providerSeasonValues =
            readProviderSeasonValues(
                leagueApiSportsId = leagueApiSportsId,
                currentSeasonYear = currentSeasonYear,
                seasonCreateDto = seasonCreateDto,
            )
        val coreSeasonIdentity =
            CoreSeasonIdentity(
                leagueCoreId = leagueCoreId,
                seasonYear = providerSeasonValues.seasonYear,
            )
        val providerSeasonIdentity =
            ProviderSeasonIdentity(
                leagueApiSportsId = leagueApiSportsId,
                seasonYear = providerSeasonValues.seasonYear,
            )

        // Season row existence cases are handled by resolving each side independently:
        //
        // - Core X / Provider X: both findOrCreate... methods create new rows.
        // - Core O / Provider X: core is reused, provider is created.
        // - Core X / Provider O: core is created, provider is reused.
        // - Core O / Provider O: both are reused.
        //
        // After both sides are known, updateProviderSeasonFields establishes or fixes
        // the provider -> core link. Link states are not separate branches here:
        // null, wrong year, wrong league, and correct link all converge in that method.
        val coreSeason =
            findOrCreateCoreSeason(
                identity = coreSeasonIdentity,
                leagueCore = leagueCore,
                coreSeasonsByIdentity = existingSeasons.coreSeasonsByIdentity,
            )
        if (updateCoreSeasonFields(coreSeason, providerSeasonValues)) {
            savePlan.coreSeasonsNeedingSave[coreSeasonIdentity] = coreSeason
        }

        val providerSeason =
            findOrCreateProviderSeason(
                identity = providerSeasonIdentity,
                leagueApiSports = leagueApiSports,
                providerSeasonsByIdentity = existingSeasons.providerSeasonsByIdentity,
            )

        // Covers Provider -> Core link cases:
        // - no link: attach the matching core season
        // - wrong link: replace with the matching core season
        // - correct link: leave as-is and only update provider fields if needed
        if (updateProviderSeasonFields(providerSeason, leagueApiSports, providerSeasonValues, coreSeason)) {
            savePlan.providerSeasonsNeedingSave[providerSeasonIdentity] = providerSeason
        }
    }

    private fun updateCurrentFlagForExistingCoreSeasons(
        leagueCoreId: Long,
        currentSeasonYear: Int?,
        coreSeasonsByIdentity: Map<CoreSeasonIdentity, LeagueSeasonCore>,
    ): List<ChangedCoreSeason> =
        coreSeasonsByIdentity
            .filterKeys { it.leagueCoreId == leagueCoreId }
            .mapNotNull { (identity, coreSeason) ->
                val shouldBeCurrent = identity.seasonYear == currentSeasonYear
                if (coreSeason.current == shouldBeCurrent) {
                    null
                } else {
                    coreSeason.current = shouldBeCurrent
                    ChangedCoreSeason(identity = identity, coreSeason = coreSeason)
                }
            }

    private fun findOrCreateCoreSeason(
        identity: CoreSeasonIdentity,
        leagueCore: LeagueCore,
        coreSeasonsByIdentity: MutableMap<CoreSeasonIdentity, LeagueSeasonCore>,
    ): LeagueSeasonCore {
        val existingCoreSeason = coreSeasonsByIdentity[identity]
        if (existingCoreSeason != null) {
            return existingCoreSeason
        }

        val createdCoreSeason =
            LeagueSeasonCore(
                league = leagueCore,
                seasonYear = identity.seasonYear,
                autoGenerated = true,
            )
        coreSeasonsByIdentity[identity] = createdCoreSeason
        return createdCoreSeason
    }

    private fun findOrCreateProviderSeason(
        identity: ProviderSeasonIdentity,
        leagueApiSports: LeagueApiSports,
        providerSeasonsByIdentity: MutableMap<ProviderSeasonIdentity, LeagueApiSportsSeason>,
    ): LeagueApiSportsSeason {
        val existingProviderSeason = providerSeasonsByIdentity[identity]
        if (existingProviderSeason != null) {
            return existingProviderSeason
        }

        val createdProviderSeason =
            LeagueApiSportsSeason(
                seasonYear = identity.seasonYear,
                leagueApiSports = leagueApiSports,
            )
        providerSeasonsByIdentity[identity] = createdProviderSeason
        return createdProviderSeason
    }

    private fun updateCoreSeasonFields(
        coreSeason: LeagueSeasonCore,
        providerSeasonValues: ProviderSeasonValues,
    ): Boolean {
        var changed = coreSeason.id == null

        if (coreSeason.seasonStart != providerSeasonValues.seasonStart) {
            coreSeason.seasonStart = providerSeasonValues.seasonStart
            changed = true
        }
        if (coreSeason.seasonEnd != providerSeasonValues.seasonEnd) {
            coreSeason.seasonEnd = providerSeasonValues.seasonEnd
            changed = true
        }
        if (coreSeason.current != providerSeasonValues.current) {
            coreSeason.current = providerSeasonValues.current
            changed = true
        }

        return changed
    }

    private fun updateProviderSeasonFields(
        providerSeason: LeagueApiSportsSeason,
        leagueApiSports: LeagueApiSports,
        providerSeasonValues: ProviderSeasonValues,
        coreSeason: LeagueSeasonCore,
    ): Boolean {
        var changed = providerSeason.id == null

        if (providerSeason.seasonYear != providerSeasonValues.seasonYear) {
            providerSeason.seasonYear = providerSeasonValues.seasonYear
            changed = true
        }
        if (providerSeason.seasonStart != providerSeasonValues.seasonStart) {
            providerSeason.seasonStart = providerSeasonValues.seasonStart
            changed = true
        }
        if (providerSeason.seasonEnd != providerSeasonValues.seasonEnd) {
            providerSeason.seasonEnd = providerSeasonValues.seasonEnd
            changed = true
        }
        if (providerSeason.coverage != providerSeasonValues.coverage) {
            providerSeason.coverage = providerSeasonValues.coverage
            changed = true
        }
        if (providerSeason.leagueApiSports?.id != leagueApiSports.id) {
            providerSeason.leagueApiSports = leagueApiSports
            changed = true
        }
        if (!sameCoreSeason(providerSeason.leagueSeasonCore, coreSeason)) {
            providerSeason.leagueSeasonCore = coreSeason
            changed = true
        }

        return changed
    }

    private fun savePlannedChanges(savePlan: SeasonSavePlan) {
        if (savePlan.coreSeasonsNeedingSave.isNotEmpty()) {
            leagueSeasonCoreRepository.saveAll(savePlan.coreSeasonsNeedingSave.values)
        }
        if (savePlan.providerSeasonsNeedingSave.isNotEmpty()) {
            leagueApiSportsSeasonRepository.saveAll(savePlan.providerSeasonsNeedingSave.values)
        }
    }

    private fun readProviderSeasonValues(
        leagueApiSportsId: Long,
        currentSeasonYear: Int?,
        seasonCreateDto: LeagueApiSportsSeasonCreateDto,
    ): ProviderSeasonValues {
        val seasonYear =
            requireNotNull(seasonCreateDto.seasonYear) {
                "LeagueApiSports seasonYear is required. leagueApiSportsId=$leagueApiSportsId"
            }
        return ProviderSeasonValues(
            seasonYear = seasonYear,
            seasonStart = seasonCreateDto.seasonStart?.let { LocalDate.parse(it) },
            seasonEnd = seasonCreateDto.seasonEnd?.let { LocalDate.parse(it) },
            coverage = seasonCreateDto.coverage?.let { createLeagueApiSportsCoverage(it) },
            current = seasonYear == currentSeasonYear,
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

    private fun sameCoreSeason(
        left: LeagueSeasonCore?,
        right: LeagueSeasonCore,
    ): Boolean {
        val leftId = left?.id
        val rightId = right.id
        return if (leftId != null && rightId != null) {
            leftId == rightId
        } else {
            left === right
        }
    }

    private fun createLeagueApiSportsCoverage(coverageCreateDto: LeagueApiSportsCoverageCreateDto) =
        LeagueApiSportsCoverage(
            fixturesEvents = coverageCreateDto.fixturesEvents,
            fixturesLineups = coverageCreateDto.fixturesLineups,
            fixturesStatistics = coverageCreateDto.fixturesStatistics,
            fixturesPlayers = coverageCreateDto.fixturesPlayers,
            standings = coverageCreateDto.standings,
            players = coverageCreateDto.players,
            topScorers = coverageCreateDto.topScorers,
            topAssists = coverageCreateDto.topAssists,
            topCards = coverageCreateDto.topCards,
            injuries = coverageCreateDto.injuries,
            predictions = coverageCreateDto.predictions,
            odds = coverageCreateDto.odds,
        )

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

    private data class ExistingSeasonRows(
        val providerSeasonsByIdentity: MutableMap<ProviderSeasonIdentity, LeagueApiSportsSeason>,
        val coreSeasonsByIdentity: MutableMap<CoreSeasonIdentity, LeagueSeasonCore>,
    )

    /**
     * Planned writes for this sync call.
     *
     * The maps also de-duplicate changes inside one call. For example, a core season can be marked
     * for save once because current changed and again because date fields changed; only one entity
     * instance should be saved for that identity.
     */
    private data class SeasonSavePlan(
        val coreSeasonsNeedingSave: MutableMap<CoreSeasonIdentity, LeagueSeasonCore> = linkedMapOf(),
        val providerSeasonsNeedingSave: MutableMap<ProviderSeasonIdentity, LeagueApiSportsSeason> = linkedMapOf(),
    )

    private data class CoreSeasonIdentity(
        val leagueCoreId: Long,
        val seasonYear: Int,
    )

    private data class ProviderSeasonIdentity(
        val leagueApiSportsId: Long,
        val seasonYear: Int,
    )

    private data class ChangedCoreSeason(
        val identity: CoreSeasonIdentity,
        val coreSeason: LeagueSeasonCore,
    )

    private data class ProviderSeasonValues(
        val seasonYear: Int,
        val seasonStart: LocalDate?,
        val seasonEnd: LocalDate?,
        val coverage: LeagueApiSportsCoverage?,
        val current: Boolean,
    )

    private data class ManagedLeagueReferences(
        val leagueApiSportsById: Map<Long, LeagueApiSports>,
        val leagueCoreById: Map<Long, LeagueCore>,
    ) {
        fun requireLeagueApiSports(leagueApiSportsId: Long): LeagueApiSports =
            requireNotNull(leagueApiSportsById[leagueApiSportsId]) {
                "LeagueApiSports reference was not prepared. leagueApiSportsId=$leagueApiSportsId"
            }

        fun requireLeagueCore(leagueCoreId: Long): LeagueCore =
            requireNotNull(leagueCoreById[leagueCoreId]) {
                "LeagueCore reference was not prepared. leagueCoreId=$leagueCoreId"
            }
    }
}

data class LeagueSeasonSyncInput(
    val leagueCreateDto: LeagueApiSportsCreateDto,
    val leagueApiSportsId: Long,
    val leagueCoreId: Long,
)
