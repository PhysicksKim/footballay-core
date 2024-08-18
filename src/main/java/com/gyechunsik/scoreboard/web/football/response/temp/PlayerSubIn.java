package com.gyechunsik.scoreboard.web.football.response.temp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PlayerSubIn {

    @Id
    public Long id;

    public Boolean isSubIn = true;
}
