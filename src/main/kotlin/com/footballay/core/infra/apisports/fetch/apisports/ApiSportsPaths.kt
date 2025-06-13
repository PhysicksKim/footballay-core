package com.footballay.core.infra.apisports.fetch.apisports

class ApiSportsPaths {
    companion object {
        /**
         * ex. ?id=1208397&timezone=Asia%2FSeoul
         *
         * timezone is optional, default is UTC
         */
        const val fixtureSingle: String = "/fixtures"

        /**
         * ex. ?league=39&season=2024
         *
         * season is essential
         */
        const val fixturesOfLeague: String = "/fixtures"

        /**
         * ex. ?current=true
         */
        const val leaguesCurrent: String = "/leagues"

        /**
         * ex. ?team=50
         */
        const val squadOfTeam: String = "/players/squads"

        /**
         * ex. ?league=39&season=2024
         *
         * season is essential
         */
        const val teamsOfLeague: String = "/teams"

        /**
         * no additional query parameters
         */
        const val status: String = "/status"
    }
}
