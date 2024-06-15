package com.gyechunsik.scoreboard.domain.football.available.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 이용 가능한 리그 id 들을 지정해둡니다.
 * 수정 삭제 편의를 위해 FK 를 등록하지 않고 독립적으로 둡니다.
 */
@Entity
@Slf4j
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableLeague extends BaseDateAuditEntity{

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueId;

    @Column(nullable = false)
    private String name;

    // @OneToOne
    // private League league;
}
