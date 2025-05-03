package com.footballay.core.domain.user.entity;

import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = true, unique = true, length = 30)
    private String nickname;

    /**
     * 프로필 이미지 URL
     */
    @Column(nullable = true)
    private String profileImage;

    @Column(nullable = false, length = 500)
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Authority> authorities = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PreferenceKey preferenceKey;

}
