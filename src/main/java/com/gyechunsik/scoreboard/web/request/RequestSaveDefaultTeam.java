package com.gyechunsik.scoreboard.web.request;

import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.TeamSide;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestSaveDefaultTeam {

    private String streamerHash;
    private String category;
    private String teamCode;
    private TeamSide side;
    private DefaultUniform uniform;
}
