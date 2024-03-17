package com.gyechunsik.scoreboard.domain.football.player.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Table(name = "players")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Player {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String koreanName;
    private String photoUrl;
    private String position;
}
