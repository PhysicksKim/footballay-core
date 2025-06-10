package com.footballay.core.infra.persistence.apisports.entity

import com.footballay.core.infra.persistence.core.entity.TeamCore
import jakarta.persistence.*

@Entity
@Table(name = "refac_team_api_sports")
data class TeamApiSports(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_core_id", referencedColumnName = "id")
    var teamCore: TeamCore? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    var venue: VenueApiSports? = null, // API 응답의 team.venue

    var apiId: Long? = null, // API 응답의 team.id
    var name: String? = null, // API 응답의 team.name
    var code: String? = null, // API 응답의 team.code
    var country: String? = null, // API 응답의 team.country
    var founded: Int? = null, // API 응답의 team.founded
    var national: Boolean? = null, // API 응답의 team.national
    var logo: String? = null, // API 응답의 team.logo

)
