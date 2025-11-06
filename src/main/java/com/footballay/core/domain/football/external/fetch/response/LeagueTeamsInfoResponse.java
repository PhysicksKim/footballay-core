package com.footballay.core.domain.football.external.fetch.response;

import java.util.List;

public class LeagueTeamsInfoResponse extends ApiFootballResponse {
    private List<_Response> response;


    public static class _Response {
        private _TeamResponse team;
        private _Venue venue;

        public _TeamResponse getTeam() {
            return this.team;
        }

        public _Venue getVenue() {
            return this.venue;
        }

        public void setTeam(final _TeamResponse team) {
            this.team = team;
        }

        public void setVenue(final _Venue venue) {
            this.venue = venue;
        }
    }


    public static class _TeamResponse {
        private int id;
        private String name;
        private String code;
        private String country;
        private int founded;
        private boolean national;
        private String logo;

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getCode() {
            return this.code;
        }

        public String getCountry() {
            return this.country;
        }

        public int getFounded() {
            return this.founded;
        }

        public boolean isNational() {
            return this.national;
        }

        public String getLogo() {
            return this.logo;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public void setCountry(final String country) {
            this.country = country;
        }

        public void setFounded(final int founded) {
            this.founded = founded;
        }

        public void setNational(final boolean national) {
            this.national = national;
        }

        public void setLogo(final String logo) {
            this.logo = logo;
        }
    }


    public static class _Venue {
        private int id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getAddress() {
            return this.address;
        }

        public String getCity() {
            return this.city;
        }

        public int getCapacity() {
            return this.capacity;
        }

        public String getSurface() {
            return this.surface;
        }

        public String getImage() {
            return this.image;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setAddress(final String address) {
            this.address = address;
        }

        public void setCity(final String city) {
            this.city = city;
        }

        public void setCapacity(final int capacity) {
            this.capacity = capacity;
        }

        public void setSurface(final String surface) {
            this.surface = surface;
        }

        public void setImage(final String image) {
            this.image = image;
        }
    }

    public List<_Response> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_Response> response) {
        this.response = response;
    }
}
