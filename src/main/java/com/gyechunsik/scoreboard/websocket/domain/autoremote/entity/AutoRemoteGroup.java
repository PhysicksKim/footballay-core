package com.gyechunsik.scoreboard.websocket.domain.autoremote.entity;

import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
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
        return "AutoRemoteGroup{" +
                "id=" + id +
                ", expiredAt=" + expiredAt +
                ", lastActiveAt=" + lastActiveAt +
                '}';
    }
}
