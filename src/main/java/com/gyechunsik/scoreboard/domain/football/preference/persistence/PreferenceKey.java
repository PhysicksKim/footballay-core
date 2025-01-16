package com.gyechunsik.scoreboard.domain.football.preference.persistence;

import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;
import org.hibernate.type.descriptor.java.BooleanJavaType;

import java.util.Objects;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "preference_keys")
@SQLDelete(sql = "UPDATE preference_keys SET enabled = false WHERE id = ?")
@FilterDef(
        name = "enabledFilter",
        parameters = @ParamDef(name = "enabled", type = BooleanJavaType.class)
)
@Filter(
        name = "enabledFilter",
        condition = "enabled = :enabled"
)
public class PreferenceKey extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String keyhash;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

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
