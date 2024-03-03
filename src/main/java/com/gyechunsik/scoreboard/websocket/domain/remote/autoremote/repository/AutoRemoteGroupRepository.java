package com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.repository;

import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AutoRemoteGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoRemoteGroupRepository extends JpaRepository<AutoRemoteGroup, Long> {
}
