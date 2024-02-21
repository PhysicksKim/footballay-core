package com.gyechunsik.scoreboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;

@SpringBootApplication
public class ScoreBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreBoardApplication.class, args);
    }

}
