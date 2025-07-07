package com.footballay.core.infra.apisports.syncer.match.dto

data class PlayerStatSyncDto (
    val homePlayerStatList: List<PlayerStatSyncItemDto>,
    val awayPlayerStatList: List<PlayerStatSyncItemDto>,
) {

    data class PlayerStatSyncItemDto(
        val playerApiId: Long?,
        val name: String,
        
        // Games statistics
        val minutesPlayed: Int? = null,
        val shirtNumber: Int? = null,
        val position: String? = null,
        val rating: Double? = null,
        val isCaptain: Boolean? = false,
        val isSubstitute: Boolean? = false,
        
        // Offsides
        val offsides: Int? = null,
        
        // Shots
        val shotsTotal: Int? = null,
        val shotsOnTarget: Int? = null,
        
        // Goals
        val goalsTotal: Int? = null,
        val goalsConceded: Int? = null,
        val assists: Int? = null,
        val saves: Int? = null,
        
        // Passes
        val passesTotal: Int? = null,
        val keyPasses: Int? = null,
        val passesAccuracy: Int? = null,
        
        // Tackles
        val tacklesTotal: Int? = null,
        val blocks: Int? = null,
        val interceptions: Int? = null,
        
        // Duels
        val duelsTotal: Int? = null,
        val duelsWon: Int? = null,
        
        // Dribbles
        val dribblesAttempts: Int? = null,
        val dribblesSuccess: Int? = null,
        val dribblesPast: Int? = null,
        
        // Fouls
        val foulsDrawn: Int? = null,
        val foulsCommitted: Int? = null,
        
        // Cards
        val yellowCards: Int = 0,
        val redCards: Int = 0,
        
        // Penalty
        val penaltyWon: Int? = null,
        val penaltyCommitted: Int? = null,
        val penaltyScored: Int = 0,
        val penaltyMissed: Int = 0,
        val penaltySaved: Int = 0
    )

    companion object {
        fun empty(): PlayerStatSyncDto {
            return PlayerStatSyncDto(emptyList(), emptyList())
        }
    }
}