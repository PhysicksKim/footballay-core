package com.gyechunsik.scoreboard.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.persistence.standings.Standing;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "league")
public class League extends BaseDateAuditEntity {

    @Id
    private Long leagueId;

    @Column(nullable = false)
    private String name;

    private String koreanName;
    private String logo;

    @Builder.Default
    private boolean available = false;

    @Column(nullable = true)
    private Integer currentSeason;

    /**
     * 리그의 standings 를 가져올 수 있는지 여부입니다.
     * 리그별로 standing 처리가 다를 수 있으므로 가능한 리그를 제한합니다.
     * @see Standing
     */
    @Builder.Default
    @Column(nullable = true)
    private boolean standingAvailable = false;

    /**
     * 하나의 리그에는 season 별로 다양한 standing 이 존재할 수 있습니다.
     */
    @Builder.Default
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Standing> standing = new ArrayList<>();

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "league")
    private List<LeagueTeam> leagueTeams;

    public void updateCompare(League other) {
        if(this.getLeagueId() != other.getLeagueId()) return;
        if(!Objects.equals(this.getName(), other.getName())) this.setName(other.getName());
        // 로고는 업데이트 하지 않음. 다른 로고를 사용할 수 있기 때문임
        // if(!Objects.equals(this.getLogo(), other.getLogo())) this.setLogo(other.getLogo());
        if(!Objects.equals(this.getCurrentSeason(), other.getCurrentSeason())) this.setCurrentSeason(other.getCurrentSeason());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        League league = (League) o;
        return Objects.equals(getLeagueId(), league.getLeagueId()) && Objects.equals(getName(), league.getName()) && Objects.equals(getKoreanName(), league.getKoreanName()) && Objects.equals(getLogo(), league.getLogo()) && Objects.equals(getCurrentSeason(), league.getCurrentSeason()) && Objects.equals(getLeagueTeams(), league.getLeagueTeams());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getLeagueId());
        result = 31 * result + Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getKoreanName());
        result = 31 * result + Objects.hashCode(getLogo());
        result = 31 * result + Objects.hashCode(getCurrentSeason());
        result = 31 * result + Objects.hashCode(getLeagueTeams());
        return result;
    }
}
