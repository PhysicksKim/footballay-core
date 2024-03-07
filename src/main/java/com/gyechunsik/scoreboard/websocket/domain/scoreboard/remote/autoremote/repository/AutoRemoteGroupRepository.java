package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoRemoteGroupRepository extends JpaRepository<AutoRemoteGroup, Long> {
}
