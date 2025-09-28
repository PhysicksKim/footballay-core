package com.footballay.core.infra.apisports.backbone.sync.fixture

import com.footballay.core.infra.apisports.backbone.sync.fixture.factory.FixtureApiSportsFactory
import com.footballay.core.infra.apisports.backbone.sync.fixture.factory.VenueApiSportsFactory
import com.footballay.core.infra.apisports.backbone.sync.fixture.model.FixtureApiSportsProcessingCases
import com.footballay.core.infra.apisports.backbone.sync.fixture.model.FixtureDataCollection
import com.footballay.core.infra.apisports.backbone.sync.fixture.model.FixtureProcessingCases
import com.footballay.core.infra.apisports.backbone.sync.fixture.model.VenueProcessingCases
import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.mapper.FixtureDataMapper
import com.footballay.core.infra.core.FixtureCoreSyncService
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.VenueApiSportsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * FixtureApiSports와 FixtureCore를 함께 동기화하는 서비스
 * 
 * TDD 방식으로 단계별 구현:
 * - Phase 1: 기본 검증 및 데이터 수집
 * - Phase 2: FixtureCore 생성/업데이트
 * - Phase 3: FixtureApiSports 생성/업데이트
 * - Phase 4: 연관관계 설정
 * - Phase 5: Venue 처리
 */
@Service
class FixtureApiSportsWithCoreSyncer(
    // ApiSports Repo'
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
    private val venueApiSportsRepository: VenueApiSportsRepository,
    // Core Service
    private val fixtureCoreSyncService: FixtureCoreSyncService,
    // Factory
    private val fixtureApiSportsFactory: FixtureApiSportsFactory,
    private val venueApiSportsFactory: VenueApiSportsFactory,
    // utils
    private val fixtureDataMapper: FixtureDataMapper,
) : FixtureApiSportsSyncer {
    
    private val log = logger()
    
    /**
     * 리그의 경기들을 동기화합니다.
     *
     * ## 사전에 필요한 엔티티
     * - [LeagueApiSports] 와 [LeagueApiSportsSeason] 이 저장되어 있어야 합니다.
     * - Fixture 에 등장하는 [TeamCore] 와 [TeamApiSports] 가 모두 저장되어 있어야 합니다.
     *
     * ## 업데이트 및 생성 대상 (순서대로)
     * - [VenueApiSports] 는 API로부터 받은 경기 장소 정보를 기반으로 생성/업데이트됩니다.
     * - [FixtureCore] 는 API로부터 받은 경기 정보를 기반으로 생성/업데이트됩니다.
     * - [FixtureApiSports] 는 API로부터 받은 경기 정보를 기반으로 생성/업데이트됩니다.
     *
     * ## DTO 조건
     * 모든 DTO는 동일한 시즌이어야 하며, 시즌 정보가 반드시 포함되어 있어야 합니다.
     *
     * @param leagueApiId 리그 API ID
     * @param dtos 동기화할 경기 DTO 목록 (모든 DTO는 동일한 시즌이어야 함)
     */
    @Transactional
    override fun saveFixturesOfLeague(
        leagueApiId: Long,
        dtos: List<FixtureApiSportsCreateDto>
    ) {
        // Phase 1: 기본 검증
        validateInput(leagueApiId, dtos)
        
        // Phase 2: 데이터 수집
        val seasonYear = dtos.first().seasonYear ?: throw IllegalArgumentException("Season year must be provided")
        val fixtureData = collectFixtureData(leagueApiId, seasonYear, dtos)
        validateMissingTeams(fixtureData, dtos)
        log.info("Phase 2 data collection completed. Found ${fixtureData.fixtures.size} fixtures, ${fixtureData.teams.size} teams")

        // Phase 3: Venue 생성/업데이트
        val venueMap = saveVenues(dtos, fixtureData)
        log.info("Phase 3 venue processing completed. Processed ${venueMap.size} venues")

        // Phase 4: Fixture 케이스 분리
        val fixtureCases = separateFixtureCases(dtos, fixtureData.fixtures)
        log.info("Phase 4 case separation completed. " +
                "Both exist: ${fixtureCases.bothExistFixtures.size}, " +
                "Api only: ${fixtureCases.apiOnlyFixtures.size}, " +
                "Both new: ${fixtureCases.bothNewDtos.size}")

        // Phase 5: FixtureCore 생성/업데이트
        val newCoreMap = saveNewCores(fixtureCases, fixtureData)
        log.info("Phase 5 core save completed. Total cores: ${newCoreMap.size}")

        // Phase 6: FixtureApiSports 생성/업데이트
        val fixtureApiSportsMap = saveFixtures(fixtureCases, fixtureData, venueMap, newCoreMap)
        log.info("Phase 6 FixtureApiSports processing completed. Processed ${fixtureApiSportsMap.size} fixtures")

        log.info("All phases completed successfully!")
    }
    
    // ============================================================================
    // Phase 1: 입력 데이터 검증
    // ============================================================================
    
    /**
     * Phase 1: 입력 데이터 검증
     *
     * 제공된 파라미터에 대한 기본 검증을 수행합니다. entity 를 실제로 조회하지 않습니다.
     */
    private fun validateInput(leagueApiId: Long, dtos: List<FixtureApiSportsCreateDto>) {
        log.info("Starting Phase 1 validation for leagueApiId: $leagueApiId with ${dtos.size} fixtures")
        
        // 1. LeagueApiId 검증
        if (leagueApiId <= 0) {
            log.error("Invalid leagueApiId: $leagueApiId. Must be positive.")
            throw IllegalArgumentException("LeagueApiId must be positive, but was: $leagueApiId")
        }
        
        // 2. DTO 목록 검증
        if (dtos.isEmpty()) {
            log.warn("Empty fixture DTO list provided for leagueApiId: $leagueApiId")
            throw IllegalArgumentException("Fixture DTO list cannot be empty")
        }
        
        // 3. Season 일관성 검증
        validateSeasonConsistency(dtos)
        
        // 4. 각 DTO 검증
        dtos.forEachIndexed { index, dto ->
            validateFixtureDto(dto, index)
        }
        
        log.info("Phase 1 validation completed successfully for leagueApiId: $leagueApiId")
    }
    
    /**
     * Season 일관성 검증
     *
     * DTO 에 담긴 season 정보가 모두 동일한지 검증합니다.
     * 여러 시즌이 섞여 있는 경우는 지원하지 않으므로 예외를 던집니다.
     */
    private fun validateSeasonConsistency(dtos: List<FixtureApiSportsCreateDto>) {
        val seasons = dtos.mapNotNull { it.seasonYear }.distinct()
        
        if (seasons.size > 1) {
            log.error("Inconsistent seasons found: $seasons. All fixtures must have the same season.")
            throw IllegalArgumentException("All fixtures must have the same season, but found: $seasons")
        }
        
        if (seasons.isEmpty()) {
            log.error("No season information found in any fixture DTO.")
            throw IllegalArgumentException("At least one fixture must have season information")
        }
        
        log.info("Season consistency validated: ${seasons.first()}")
    }
    
    /**
     * 개별 FixtureApiSportsCreateDto 검증
     *
     * homeTeam, awayTeam 의 apiId 는 nullable 입니다. 예를 들어 토너먼트 결승전 같은 경우, 경기 일정은 정해졌으나 팀이 미정일 수 있습니다.
     */
    private fun validateFixtureDto(dto: FixtureApiSportsCreateDto, index: Int) {
        // apiId 검증
        val apiId = dto.apiId
        if (apiId == null || apiId <= 0) {
            log.error("Invalid apiId at index $index: $apiId. Must be positive.")
            throw IllegalArgumentException("Fixture apiId must be positive, but was: $apiId at index $index")
        }
        
        // homeTeam apiId 검증 (nullable)
        dto.homeTeam?.apiId?.let { homeTeamApiId ->
            if (homeTeamApiId <= 0) {
                log.error("Invalid homeTeam apiId at index $index: $homeTeamApiId. Must be positive.")
                throw IllegalArgumentException("HomeTeam apiId must be positive, but was: $homeTeamApiId at index $index")
            }
        }
        
        // awayTeam apiId 검증 (nullable)
        dto.awayTeam?.apiId?.let { awayTeamApiId ->
            if (awayTeamApiId <= 0) {
                log.error("Invalid awayTeam apiId at index $index: $awayTeamApiId. Must be positive.")
                throw IllegalArgumentException("AwayTeam apiId must be positive, but was: $awayTeamApiId at index $index")
            }
        }
    }

    // ============================================================================
    // Phase 2: 기존 엔티티 데이터 수집
    // ============================================================================
    
    /**
     * Phase 2: 기존 엔티티 데이터 수집
     *
     * API 데이터 동기화 작업에 필요한 기존에 저장된 엔티티들을 미리 수집합니다.
     * 이후 이뤄지는 작업에서 추가 조회를 막기 위해, 이 메서드에서 수집하여 반환한 [FixtureDataCollection] 에 담긴 엔티티를 사용해야 합니다.
     * [FixtureDataCollection] 사용시, 반드시 주석이나 본 메서드를 참고하여 Eager Fetch 된 연관관계 여부를 확인하세요.
     *
     * 주어진 Fixture dto 들의 `apiId` 와 league 의 `seasonYear` 를 기준으로 [FixtureApiSports] 엔티티를 수집합니다.
     * 주어진 Fixture dto 들의 `homeTeam.apiId` 및 `awayTeam.apiId` 와 기존에 저장된 league season fixture 들에 담긴 Team 들을 기준으로 [TeamApiSports] 엔티티를 수집합니다.
     *
     * 사전에 [LeagueApiSports] 와 [LeagueApiSportsSeason] 이 저장되어 있지 않은 경우 예외를 던집니다.
     *
     * @see FixtureDataCollection
     */
    private fun collectFixtureData(
        leagueApiId: Long, 
        seasonYear: String, 
        dtos: List<FixtureApiSportsCreateDto>
    ): FixtureDataCollection {
        log.info("Starting Phase 2 data collection for leagueApiId: $leagueApiId, seasonYear: $seasonYear")
        
        // 조회1: League 관련 데이터
        val league = leagueApiSportsRepository.findByApiIdAndSeasonWithCoreAndSeasons(leagueApiId, seasonYear)
            ?: throw IllegalStateException("League not found with apiId: $leagueApiId and season: $seasonYear")
        log.info("Found league: ${league.name} with ${league.seasons.size} seasons")
        
        // DTO에서 Fixture ApiId 추출
        val dtoFixtureApiIds = dtos.mapNotNull { it.apiId }
        log.info("Extracted ${dtoFixtureApiIds.size} fixture ApiIds from DTOs")
        
        // 조회2: Fixture 데이터 (League+Season OR ApiId 기반)
        val fixtures = fixtureApiSportsRepository.findFixturesByLeagueSeasonOrApiIds(
            leagueApiId, seasonYear, dtoFixtureApiIds
        )
        
        log.info("Found ${fixtures.size} fixtures (league+season or apiId based)")
        
        // Team ID 추출 (기존 Fixture 에서)
        val fixtureTeamApiSportsIds = fixtures.flatMap { fixture ->
            listOfNotNull(
                fixture.homeTeam?.id,
                fixture.awayTeam?.id
            )
        }.distinct()
        
        // DTO에서 Team ApiId 추출
        val dtoTeamApiIds = dtos.flatMap { dto ->
            listOfNotNull(
                dto.homeTeam?.apiId,
                dto.awayTeam?.apiId
            )
        }.distinct()
        
        log.info("Extracted ${fixtureTeamApiSportsIds.size} team PKs from fixtures, ${dtoTeamApiIds.size} team ApiIds from DTOs")
        
        // 조회3: Team 데이터 (PK OR ApiId 기반)
        val teams = if (fixtureTeamApiSportsIds.isNotEmpty() || dtoTeamApiIds.isNotEmpty()) {
            teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(fixtureTeamApiSportsIds, dtoTeamApiIds)
        } else {
            emptyList()
        }
        log.info("Found ${teams.size} teams with core data")
        return FixtureDataCollection(
            league = league,
            fixtures = fixtures,
            teams = teams
        )
    }

    // ============================================================================
    // Phase 3: Venue 처리
    // ============================================================================
    
    /**
     * Phase 3: Venue 저장/업데이트
     *
     * 제공된 Fixture DTO 들을 기반으로 VenueApiSports 엔티티를 생성/업데이트합니다.
     *
     */
    private fun saveVenues(
        dtos: List<FixtureApiSportsCreateDto>,
        fixtureData: FixtureDataCollection
    ): Map<Long, VenueApiSports> {
        log.info("Starting Phase 3 venue processing")
        
        // 1) Venue 케이스 분리
        val venueCases = separateVenueCases(dtos, fixtureData)
        log.info("Venue cases separated: " +
                "New: ${venueCases.newVenues.size}, " +
                "Update: ${venueCases.updateVenues.size}, " +
                "PreventUpdate: ${venueCases.preventUpdateVenues.size}")
        
        // 2) Venue 배치 저장/업데이트
        val venueMap = saveVenuesBatch(venueCases)
        
        // 3) 상세 로깅
        val allVenueApiIds = venueMap.keys.sorted()
        log.info("Phase 3 venue processing completed. " +
                "Total venues: ${venueMap.size}, " +
                "API IDs: $allVenueApiIds")
        
        return venueMap
    }
    
    /**
     * Venue 케이스 분리
     */
    private fun separateVenueCases(
        dtos: List<FixtureApiSportsCreateDto>,
        fixtureData: FixtureDataCollection
    ): VenueProcessingCases {
        val newVenues = mutableListOf<VenueOfFixtureApiSportsCreateDto>()
        val updateVenues = mutableListOf<Pair<VenueApiSports, VenueOfFixtureApiSportsCreateDto>>()
        val preventUpdateVenues = mutableListOf<VenueApiSports>()
        
        // 기존 Venue를 apiId로 매핑
        val existingVenuesMap = fixtureData.fixtures
            .mapNotNull { it.venue }
            .associateBy { it.apiId }
        
        dtos.forEach { dto ->
            dto.venue?.let { venueDto ->
                val existingVenue = existingVenuesMap[venueDto.apiId]
                
                if (existingVenue != null) {
                    if (!existingVenue.preventUpdate) {
                        updateVenues.add(existingVenue to venueDto)
                    } else {
                        preventUpdateVenues.add(existingVenue)
                    }
                } else {
                    newVenues.add(venueDto)
                }
            }
        }
        
        return VenueProcessingCases(newVenues, updateVenues, preventUpdateVenues)
    }
    
    /**
     * Venue 배치 저장/업데이트
     */
    private fun saveVenuesBatch(venueCases: VenueProcessingCases): Map<Long, VenueApiSports> {
        val resultMap = mutableMapOf<Long, VenueApiSports>()
        
        // 1. 새 Venue 생성
        val newVenues = venueCases.newVenues.map { dto ->
            createVenueApiSports(dto)
        }
        
        // 2. 기존 Venue 업데이트
        val updatedVenues = venueCases.updateVenues.map { (existingVenue, dto) ->
            updateVenueApiSports(existingVenue, dto)
        }
        
        // 3. Venue 배치 저장
        val savedVenues = if (newVenues.isNotEmpty() || updatedVenues.isNotEmpty()) {
            venueApiSportsRepository.saveAll(newVenues + updatedVenues)
        } else {
            emptyList()
        }
        
        // 4. preventUpdate Venue들 (업데이트하지 않지만 맵에 포함)
        val preventUpdateVenues = venueCases.preventUpdateVenues
        
        // 5. 결과 맵 구성
        (savedVenues + preventUpdateVenues).forEach { venue ->
            venue.apiId?.let { apiId ->
                resultMap[apiId] = venue
            }
        }
        
        return resultMap
    }
    
        /**
     * 새 VenueApiSports 생성
     */
    private fun createVenueApiSports(dto: VenueOfFixtureApiSportsCreateDto): VenueApiSports {
        return venueApiSportsFactory.createVenueApiSports(dto)
    }

    /**
     * 기존 VenueApiSports 업데이트
     */
    private fun updateVenueApiSports(
        existingVenue: VenueApiSports,
        dto: VenueOfFixtureApiSportsCreateDto
    ): VenueApiSports {
        return venueApiSportsFactory.updateVenueApiSports(existingVenue, dto)
    }

    // ============================================================================
    // Phase 4: Fixture 케이스 분리
    // ============================================================================
    
    /**
     * Phase 4: Fixture 케이스 분리
     * 
     * 입력된 Fixture DTO들과 기존 FixtureApiSports 엔티티들을 비교하여 3가지 케이스로 분류합니다:
     * 1. bothExistFixtures: FixtureApiSports와 FixtureCore가 모두 존재하는 경우 (업데이트 대상)
     * 2. apiOnlyFixtures: FixtureApiSports는 존재하지만 FixtureCore가 없는 경우 (Core 연결 대상)
     * 3. bothNewDtos: FixtureApiSports와 FixtureCore가 모두 없는 경우 (새로 생성 대상)
     * 
     * @param dtos 처리할 Fixture DTO 목록
     * @param existingFixtures 기존 FixtureApiSports 엔티티 목록
     * @return 분류된 케이스들
     */
    private fun separateFixtureCases(
        dtos: List<FixtureApiSportsCreateDto>,
        existingFixtures: List<FixtureApiSports>
    ): FixtureProcessingCases {
        val bothExistFixtures = mutableListOf<FixtureApiSports>()
        val apiOnlyFixtures = mutableListOf<Pair<FixtureApiSports, FixtureApiSportsCreateDto>>()
        val bothNewDtos = mutableListOf<FixtureApiSportsCreateDto>()
        
        // 기존 FixtureApiSports를 apiId로 매핑
        val existingFixturesMap = existingFixtures.associateBy { it.apiId }
        
        dtos.forEach { dto ->
            val existingFixture = existingFixturesMap[dto.apiId]
            
            if (existingFixture != null) {
                if (existingFixture.core != null) {
                    // FixtureApiSports O, FixtureCore O
                    bothExistFixtures.add(existingFixture)
                } else {
                    // FixtureApiSports O, FixtureCore X
                    apiOnlyFixtures.add(existingFixture to dto)
                }
            } else {
                // FixtureApiSports X, FixtureCore X
                bothNewDtos.add(dto)
            }
        }
        
        log.info("Fixture cases separated: " +
                "Both exist: ${bothExistFixtures.size}, " +
                "Api only: ${apiOnlyFixtures.size}, " +
                "Both new: ${bothNewDtos.size}")
        
        return FixtureProcessingCases(bothExistFixtures, apiOnlyFixtures, bothNewDtos)
    }

    // ============================================================================
    // Phase 5: FixtureCore 저장
    // ============================================================================
    
    /**
     * Phase 5: 새로운 FixtureCore 저장
     * 
     * Identity Pairing Pattern을 사용하여 UID를 생성하고 FixtureCore를 생성합니다.
     * 
     * @param fixtureCases 분리된 Fixture 케이스들
     * @param fixtureData 수집된 데이터
     * @return ApiId -> FixtureCore 맵 (영속 상태)
     */
    private fun saveNewCores(
        fixtureCases: FixtureProcessingCases,
        fixtureData: FixtureDataCollection
    ): Map<Long, FixtureCore> {
        log.info("starting new core save process")

        val coreCreateDtos = mutableListOf<FixtureApiSportsCreateDto>()
        fixtureCases.apiOnlyFixtures.forEach { (_,dto) -> coreCreateDtos.add(dto) }
        coreCreateDtos.addAll(fixtureCases.bothNewDtos)

        if (coreCreateDtos.isEmpty()) {
            log.info("No new FixtureCore DTOs to create. Skipping core creation.")
            return emptyMap()
        }
        log.info("Collected ${coreCreateDtos.size} DTOs for core creation")

        val uidPairs = fixtureCoreSyncService.generateUidPairs(coreCreateDtos)
        log.info("Generated ${uidPairs.size} UID pairs")

        val uidToApiIdMap = uidPairs.associate { (uid, dto) ->
            uid to (dto.apiId ?: throw IllegalArgumentException("ApiId is required for core creation"))
        }

        val teamMap = fixtureData.teams.filter { it.apiId != null }.associateBy { it.apiId!! }

        val leagueCore = fixtureData.league.leagueCore ?: throw IllegalStateException("LeagueCore must not be null for core creation")
        val coreCreatePairs = uidPairs.map { (uid, dto) ->
            val homeTeam = dto.homeTeam?.apiId?.let { teamMap[it] }?.teamCore
            val awayTeam = dto.awayTeam?.apiId?.let { teamMap[it] }?.teamCore

            val coreDto = fixtureDataMapper.toFixtureCoreCreateDto(
                uid = uid,
                dto = dto,
                league = leagueCore,
                homeTeam = homeTeam,
                awayTeam = awayTeam
            )
            uid to coreDto
        }

        val savedCoreMap = fixtureCoreSyncService.createFixtureCores(coreCreatePairs)
        log.info("Successfully created ${savedCoreMap.size} FixtureCores")

        val resultMap = mutableMapOf<Long, FixtureCore>()

        savedCoreMap.entries.forEach { (uid, core) ->
            val apiId = uidToApiIdMap[uid]
                ?: throw IllegalStateException("UID $uid not found in mapping table")
            resultMap[apiId] = core
        }

        fixtureData.fixtures.forEach { fixture ->
            fixture.core?.let { core ->
                resultMap[fixture.apiId] = core
            }
        }

        log.info("Phase 5 core save completed. Total cores: ${resultMap.size} (New: ${savedCoreMap.size}, Existing: ${fixtureData.fixtures.count { it.core != null }})")
        return resultMap.toMap() // 불변 Map으로 변환
    }

    // ============================================================================
    // Phase 6: FixtureApiSports 저장/업데이트
    // ============================================================================
    
    /**
     * Phase 6: FixtureApiSports 저장/업데이트
     * 
     * Venue와 유사한 패턴으로 FixtureApiSports를 처리하되,
     * FixtureCore FK를 바로 설정하여 성능을 최적화합니다.
     * 
     * @param fixtureCases 분리된 Fixture 케이스들
     * @param fixtureData 수집된 데이터
     * @param venueMap VenueApiSports 맵 (Phase 3에서 생성)
     * @param coreMap FixtureCore 맵 (Phase 5에서 생성)
     * @return ApiId -> FixtureApiSports 맵 (영속 상태)
     */
    private fun saveFixtures(
        fixtureCases: FixtureProcessingCases,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): Map<Long, FixtureApiSports> {
        log.info("Starting Phase 6 FixtureApiSports processing")
        
        // 1) FixtureApiSports 케이스 분리
        val fixtureApiSportsCases = separateFixtureApiSportsCases(fixtureCases)
        log.info("FixtureApiSports cases separated: " +
                "New: ${fixtureApiSportsCases.newFixtures.size}, " +
                "Update: ${fixtureApiSportsCases.updateFixtures.size}, " +
                "PreventUpdate: ${fixtureApiSportsCases.preventUpdateFixtures.size}")
        
        // 2) FixtureApiSports 배치 저장/업데이트
        val fixtureApiSportsMap = saveFixtureApiSportsBatch(fixtureApiSportsCases, fixtureData, venueMap, coreMap)
        
        // 3) 상세 로깅
        val allApiIds = fixtureApiSportsMap.keys.sorted()
        log.info("Phase 6 FixtureApiSports processing completed. " +
                "Total fixtures: ${fixtureApiSportsMap.size}, " +
                "API IDs: $allApiIds")
        
        return fixtureApiSportsMap
    }
    
    /**
     * FixtureApiSports 케이스 분리
     * 
     * FixtureProcessingCases를 기반으로 preventUpdate 플래그를 고려하여
     * 새로 생성, 업데이트, preventUpdate 케이스로 분리합니다.
     * 
     * @param fixtureCases 이미 분리된 Fixture 케이스들
     * @return preventUpdate를 고려한 FixtureApiSports 처리 케이스들
     */
    private fun separateFixtureApiSportsCases(
        fixtureCases: FixtureProcessingCases
    ): FixtureApiSportsProcessingCases {
        val newFixtures = mutableListOf<FixtureApiSportsCreateDto>()
        val updateFixtures = mutableListOf<Pair<FixtureApiSports, FixtureApiSportsCreateDto>>()
        val preventUpdateFixtures = mutableListOf<FixtureApiSports>()
        
        // bothExistFixtures: 업데이트 대상 (preventUpdate 체크)
        fixtureCases.bothExistFixtures.forEach { existingFixture ->
            val dto = fixtureCases.apiOnlyFixtures.find { (fixture, _) -> fixture.apiId == existingFixture.apiId }?.second
                ?: fixtureCases.bothNewDtos.find { it.apiId == existingFixture.apiId }
                ?: throw IllegalStateException("DTO not found for existing fixture with API ID: ${existingFixture.apiId}")
            
            if (!existingFixture.preventUpdate) {
                updateFixtures.add(existingFixture to dto)
            } else {
                preventUpdateFixtures.add(existingFixture)
            }
        }
        
        // apiOnlyFixtures: Core FK 연결 대상
        fixtureCases.apiOnlyFixtures.forEach { (existingFixture, dto) ->
            if (!existingFixture.preventUpdate) {
                updateFixtures.add(existingFixture to dto)
            } else {
                preventUpdateFixtures.add(existingFixture)
            }
        }
        
        // bothNewDtos: 새로 생성 대상
        newFixtures.addAll(fixtureCases.bothNewDtos)
        
        return FixtureApiSportsProcessingCases(newFixtures, updateFixtures, preventUpdateFixtures)
    }
    
    /**
     * FixtureApiSports 배치 저장/업데이트
     */
    private fun saveFixtureApiSportsBatch(
        fixtureCases: FixtureApiSportsProcessingCases,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): Map<Long, FixtureApiSports> {
        val resultMap = mutableMapOf<Long, FixtureApiSports>()
        
        // 1. 새 FixtureApiSports 생성
        val newFixtures = fixtureCases.newFixtures.map { dto ->
            createFixtureApiSports(dto, fixtureData, venueMap, coreMap)
        }
        
        // 2. 기존 FixtureApiSports 업데이트
        val updatedFixtures = fixtureCases.updateFixtures.map { (existingFixture, dto) ->
            updateFixtureApiSports(existingFixture, dto, fixtureData, venueMap, coreMap)
        }
        
        // 3. FixtureApiSports 배치 저장
        val savedFixtures = if (newFixtures.isNotEmpty() || updatedFixtures.isNotEmpty()) {
            fixtureApiSportsRepository.saveAll(newFixtures + updatedFixtures)
        } else {
            emptyList()
        }
        
        // 4. preventUpdate FixtureApiSports들 (업데이트하지 않지만 맵에 포함)
        val preventUpdateFixtures = fixtureCases.preventUpdateFixtures
        
        // 5. 결과 맵 구성
        (savedFixtures + preventUpdateFixtures).forEach { fixture ->
            resultMap[fixture.apiId] = fixture
        }
        
        return resultMap
    }
    
        /**
     * 새 FixtureApiSports 생성
     */
    private fun createFixtureApiSports(
        dto: FixtureApiSportsCreateDto,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): FixtureApiSports {
        return fixtureApiSportsFactory.createFixtureApiSports(dto, fixtureData, venueMap, coreMap)
    }

    /**
     * 기존 FixtureApiSports 업데이트
     */
    private fun updateFixtureApiSports(
        existingFixture: FixtureApiSports,
        dto: FixtureApiSportsCreateDto,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): FixtureApiSports {
        return fixtureApiSportsFactory.updateFixtureApiSports(existingFixture, dto, fixtureData, venueMap, coreMap)
    }

    // ============================================================================
    // 유틸리티 메서드
    // ============================================================================
    
    /**
     * 누락된 팀 검증
     *
     * DTO 에 존재하는 팀이지만 데이터베이스에 없는 경우 예외를 던집니다.
     */
    fun validateMissingTeams(
        fixtureData: FixtureDataCollection,
        dtos: List<FixtureApiSportsCreateDto>
    ) {
        val dtoTeamApiIds = mutableSetOf<Long>()
        val entityTeamApiIds = mutableSetOf<Long>()

        dtos.forEach { fixture ->
            if(fixture.homeTeam?.apiId != null) { dtoTeamApiIds.add(fixture.homeTeam!!.apiId!!) }
            if(fixture.awayTeam?.apiId != null) { dtoTeamApiIds.add(fixture.awayTeam!!.apiId!!) }
        }

        fixtureData.teams.forEach { team ->
            if(team.apiId != null) { entityTeamApiIds.add(team.apiId!!) }
        }

        val missingTeams = dtoTeamApiIds - entityTeamApiIds
        if (missingTeams.isNotEmpty()) {
            log.warn("Missing teams for API IDs: $missingTeams")
            throw IllegalStateException("Some teams are missing in the database: $missingTeams")
        }
    }
} 