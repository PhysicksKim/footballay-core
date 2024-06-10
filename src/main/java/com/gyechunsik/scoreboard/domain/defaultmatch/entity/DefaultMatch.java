package com.gyechunsik.scoreboard.domain.defaultmatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "default_matches")
public class DefaultMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "streamer_hash", referencedColumnName = "hash")
    private Streamer streamer;

    public DefaultMatch(String name, Streamer streamer) {
        this.name = name;
        this.streamer = streamer;
    }

    protected DefaultMatch() {

    }
}
