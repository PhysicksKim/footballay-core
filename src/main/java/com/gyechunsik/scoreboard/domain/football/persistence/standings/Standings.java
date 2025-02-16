package com.gyechunsik.scoreboard.domain.football.persistence.standings;

import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "standings")
public class Standings extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 API에서 받아온 리그 정보와 시즌을 참조 (기존 leagues 엔티티와 연관관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league; // 기존 PlantUML의 leagues와 매핑

    private int season;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "standings", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandingEntry> entries = new ArrayList<>();

}