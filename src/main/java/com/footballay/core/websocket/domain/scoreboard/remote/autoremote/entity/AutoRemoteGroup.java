package com.footballay.core.websocket.domain.scoreboard.remote.autoremote.entity;

import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class AutoRemoteGroup extends BaseDateAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime expiredAt;
    @Column(nullable = false)
    private LocalDateTime lastActiveAt;
    @OneToMany(mappedBy = "autoRemoteGroup")
    private Set<AnonymousUser> anonymousUsers = new HashSet<>();

    @Override
    public String toString() {
        return "AutoRemoteGroup{" + "id=" + id + ", expiredAt=" + expiredAt + ", lastActiveAt=" + lastActiveAt + '}';
    }

    public Long getId() {
        return this.id;
    }

    public LocalDateTime getExpiredAt() {
        return this.expiredAt;
    }

    public LocalDateTime getLastActiveAt() {
        return this.lastActiveAt;
    }

    public Set<AnonymousUser> getAnonymousUsers() {
        return this.anonymousUsers;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setExpiredAt(final LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public void setLastActiveAt(final LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public void setAnonymousUsers(final Set<AnonymousUser> anonymousUsers) {
        this.anonymousUsers = anonymousUsers;
    }
}
