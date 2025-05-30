package com.footballay.core.websocket.domain.scoreboard.remote.autoremote.entity;

import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "anonymous_users")
@Entity
public class AnonymousUser extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime lastConnectedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autoRemoteGroupId")
    private AutoRemoteGroup autoRemoteGroup;

    @Override
    public String toString() {
        return "AnonymousUser{" +
                "id=" + id +
                ", lastConnectedAt=" + lastConnectedAt +
                ", autoRemoteGroup=" + autoRemoteGroup.getId() +
                '}';
    }
}
