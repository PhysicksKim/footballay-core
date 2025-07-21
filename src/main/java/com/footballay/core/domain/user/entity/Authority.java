package com.footballay.core.domain.user.entity;

import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "authorities", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "authority"})})
public class Authority extends BaseDateAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role authority;

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public Role getAuthority() {
        return this.authority;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setAuthority(final Role authority) {
        this.authority = authority;
    }
}
