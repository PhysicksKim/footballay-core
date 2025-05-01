package com.footballay.core.domain.user.entity;

import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "authorities",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "authority"})})
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

}