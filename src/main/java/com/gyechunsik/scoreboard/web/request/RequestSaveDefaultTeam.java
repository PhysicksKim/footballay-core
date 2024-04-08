package com.gyechunsik.scoreboard.web.request;

import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

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
