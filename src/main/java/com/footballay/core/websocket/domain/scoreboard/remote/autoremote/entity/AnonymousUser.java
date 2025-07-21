package com.footballay.core.websocket.domain.scoreboard.remote.autoremote.entity;

import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
        return "AnonymousUser{" + "id=" + id + ", lastConnectedAt=" + lastConnectedAt + ", autoRemoteGroup=" + autoRemoteGroup.getId() + '}';
    }

    public UUID getId() {
        return this.id;
    }

    public LocalDateTime getLastConnectedAt() {
        return this.lastConnectedAt;
    }

    public AutoRemoteGroup getAutoRemoteGroup() {
        return this.autoRemoteGroup;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setLastConnectedAt(final LocalDateTime lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
    }

    public void setAutoRemoteGroup(final AutoRemoteGroup autoRemoteGroup) {
        this.autoRemoteGroup = autoRemoteGroup;
    }
}
