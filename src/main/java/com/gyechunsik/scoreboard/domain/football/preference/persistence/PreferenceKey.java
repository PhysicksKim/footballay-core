package com.gyechunsik.scoreboard.domain.football.preference.persistence;

import com.gyechunsik.scoreboard.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "preference_keys")
public class PreferenceKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String keyhash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreferenceKey that = (PreferenceKey) o;
        return Objects.equals(getKeyhash(), that.getKeyhash());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKeyhash());
    }

    @Override
    public String toString() {
        return "PreferenceKey{" +
                "id=" + id +
                ", keyhash='" + keyhash + '\'' +
                '}';
    }
}
