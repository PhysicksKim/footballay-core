package com.gyechunsik.scoreboard.websocket.domain.autoremote.repository;

import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AutoRemoteGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoRemoteGroupRepository extends JpaRepository<AutoRemoteGroup, Long> {
}
