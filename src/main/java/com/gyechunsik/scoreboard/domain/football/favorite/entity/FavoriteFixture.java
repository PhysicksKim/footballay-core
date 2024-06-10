package com.gyechunsik.scoreboard.domain.football.favorite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * 자주 찾는 리그 id 들을 지정해둡니다.
 * FK 를 등록하지 않고 독립적으로 둡니다.
 */
@Entity
@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FAVORITE_FIXTURES")
public class FavoriteFixture {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fixtureId;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Column(nullable = false)
    private String shortStatus;

    @Column(nullable = false)
    private Long leagueId;

    @Column(nullable = false)
    private String leagueName;
    private String leagueKoreanName;

    @Column(nullable = false)
    private Integer leagueSeason;

    @Column(nullable = false)
    private Long homeTeamId;
    @Column(nullable = false)
    private String homeTeamName;
    private String homeTeamKoreanName;

    @Column(nullable = false)
    private Long awayTeamId;
    @Column(nullable = false)
    private String awayTeamName;
    private String awayTeamKoreanName;
}
