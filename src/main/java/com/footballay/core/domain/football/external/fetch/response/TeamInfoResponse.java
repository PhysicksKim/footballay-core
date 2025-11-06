package com.footballay.core.domain.football.external.fetch.response;

import java.util.List;

public class TeamInfoResponse extends ApiFootballResponse {
    private List<_TeamInfo> response;


    public static class _TeamInfo {
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

        @java.lang.Override
        public java.lang.String toString() {
            return "TeamInfoResponse._TeamInfo(team=" + this.getTeam() + ", venue=" + this.getVenue() + ")";
        }
    }


    public static class _TeamResponse {
        private long id;
        private String name;
        private String code;
        private String country;
        private int founded;
        private boolean national;
        private String logo;

        public long getId() {
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

        public void setId(final long id) {
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

        @java.lang.Override
        public java.lang.String toString() {
            return "TeamInfoResponse._TeamResponse(id=" + this.getId() + ", name=" + this.getName() + ", code=" + this.getCode() + ", country=" + this.getCountry() + ", founded=" + this.getFounded() + ", national=" + this.isNational() + ", logo=" + this.getLogo() + ")";
        }
    }


    public static class _Venue {
        private long id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;

        public long getId() {
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

        public void setId(final long id) {
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

        @java.lang.Override
        public java.lang.String toString() {
            return "TeamInfoResponse._Venue(id=" + this.getId() + ", name=" + this.getName() + ", address=" + this.getAddress() + ", city=" + this.getCity() + ", capacity=" + this.getCapacity() + ", surface=" + this.getSurface() + ", image=" + this.getImage() + ")";
        }
    }

    public List<_TeamInfo> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_TeamInfo> response) {
        this.response = response;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "TeamInfoResponse(response=" + this.getResponse() + ")";
    }
}
