package com.gyechunsik.scoreboard.websocket.repository;

import com.gyechunsik.scoreboard.websocket.entity.WebsocketUser;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocketUserRepository extends JpaRepository<WebsocketUser, Long> {

}
