package com.footballay.core.domain.football.external.fetch.response;

import java.util.List;

public class PlayerSquadResponse extends ApiFootballResponse {
    private List<_TeamSquad> response;


    public static class _TeamSquad {
        private _ResponseTeam team;
        private List<_PlayerData> players;

        public _ResponseTeam getTeam() {
            return this.team;
        }

        public List<_PlayerData> getPlayers() {
            return this.players;
        }

        public void setTeam(final _ResponseTeam team) {
            this.team = team;
        }

        public void setPlayers(final List<_PlayerData> players) {
            this.players = players;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerSquadResponse._TeamSquad(team=" + this.getTeam() + ")";
        }
    }


    public static class _ResponseTeam {
        private long id;
        private String name;
        private String logo;

        public long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
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

        public void setLogo(final String logo) {
            this.logo = logo;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerSquadResponse._ResponseTeam(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ")";
        }
    }


    public static class _PlayerData {
        private long id;
        private String name;
        private int age;
        private Integer number; // 선수 등번호는 시즌 전에 캐싱하는 경우 null 이 될 수 있으므로, Wrapper Class 로 선언합니다
        private String position;
        private String photo;

        public long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }

        public Integer getNumber() {
            return this.number;
        }

        public String getPosition() {
            return this.position;
        }

        public String getPhoto() {
            return this.photo;
        }

        public void setId(final long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setAge(final int age) {
            this.age = age;
        }

        public void setNumber(final Integer number) {
            this.number = number;
        }

        public void setPosition(final String position) {
            this.position = position;
        }

        public void setPhoto(final String photo) {
            this.photo = photo;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerSquadResponse._PlayerData(id=" + this.getId() + ", name=" + this.getName() + ", age=" + this.getAge() + ", number=" + this.getNumber() + ", position=" + this.getPosition() + ", photo=" + this.getPhoto() + ")";
        }

        public _PlayerData() {
        }

        public _PlayerData(final long id, final String name, final int age, final Integer number, final String position, final String photo) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.number = number;
            this.position = position;
            this.photo = photo;
        }
    }

    public List<_TeamSquad> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_TeamSquad> response) {
        this.response = response;
    }
}
