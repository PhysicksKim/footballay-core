package com.gyechunsik.scoreboard.websocket.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class WebsocketUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String code;
    private String websocketSessionId;

}
