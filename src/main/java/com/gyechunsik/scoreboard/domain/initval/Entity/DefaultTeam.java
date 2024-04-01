package com.gyechunsik.scoreboard.domain.initval.Entity;

import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "default_teams")
public class DefaultTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TeamSide side;

    @Enumerated(EnumType.STRING)
    private LeagueCategory category;

    @Enumerated(EnumType.STRING)
    private DefaultTeamCodes code;

    @Enumerated(EnumType.STRING)
    private DefaultUniform uniform;

    @ManyToOne
    @JoinColumn(name = "streamer_hash", referencedColumnName = "hash")
    private Streamer streamer;

    public DefaultTeam(TeamSide side, LeagueCategory category, DefaultTeamCodes code, DefaultUniform uniform, Streamer streamer) {
        this.side = side;
        this.category = category;
        this.code = code;
        this.uniform = uniform;
        this.streamer = streamer;
    }

    protected DefaultTeam() {
    }
}
