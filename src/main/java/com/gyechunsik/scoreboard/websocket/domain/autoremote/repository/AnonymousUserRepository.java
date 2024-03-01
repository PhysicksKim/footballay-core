package com.gyechunsik.scoreboard.websocket.domain.autoremote.repository;

import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AnonymousUser;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface AnonymousUserRepository extends JpaRepository<AnonymousUser, UUID> {

}
