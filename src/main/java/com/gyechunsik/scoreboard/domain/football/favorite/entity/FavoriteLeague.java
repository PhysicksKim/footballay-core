package com.gyechunsik.scoreboard.domain.football.favorite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 자주 찾는 리그 id 들을 지정해둡니다.
 * FK 를 등록하지 않고 독립적으로 둡니다.
 */
@Entity
@Slf4j
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FAVORITE_LEAGUES")
public class FavoriteLeague extends BaseDateAuditEntity {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueId;

    @Column(nullable = false)
    private Integer season;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String koreanName;

}
