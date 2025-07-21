package com.footballay.core.domain.football.external.fetch.response;

public class _LeagueResponse {
    protected long id;
    protected String name;
    protected String type;
    protected String logo;

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
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

    public void setType(final String type) {
        this.type = type;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "_LeagueResponse(id=" + this.getId() + ", name=" + this.getName() + ", type=" + this.getType() + ", logo=" + this.getLogo() + ")";
    }
}
