package com.gyechunsik.scoreboard.domain.football.preference.persistence;

import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.descriptor.java.BooleanJavaType;

import java.util.Objects;

/**
 * 사용자의 환경설정 키를 저장하는 엔티티입니다. <br>
 * 한국어로 "속성키" 라는 이름을 사용합니다. <br>
 * DB 세팅 시 keyhash 가 대소문자를 구분하도록 table 이 대소문자를 구분 하도록 collation 을 설정해야 합니다. (ex.utf8mb4_bin) <br>
 */
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
        name = "SoftDeleteFilter",
        parameters = @ParamDef(name = "softDeleteBool", type = BooleanJavaType.class)
)
@Filter(
        name = "SoftDeleteFilter",
        condition = "enabled = :softDeleteBool"
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
