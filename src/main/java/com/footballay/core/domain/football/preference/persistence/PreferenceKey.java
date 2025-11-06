package com.footballay.core.domain.football.preference.persistence;

import com.footballay.core.domain.user.entity.User;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
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
@Entity
@Table(name = "preference_keys")
@SQLDelete(sql = "UPDATE preference_keys SET enabled = false WHERE id = ?")
@FilterDef(name = "SoftDeleteFilter", parameters = @ParamDef(name = "softDeleteBool", type = BooleanJavaType.class))
@Filter(name = "SoftDeleteFilter", condition = "enabled = :softDeleteBool")
public class PreferenceKey extends BaseDateAuditEntity {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PreferenceKey.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true)
    private String keyhash;
    @Column(nullable = false)
    private boolean enabled;

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
        return "PreferenceKey{" + "id=" + id + ", keyhash=\'" + keyhash + '\'' + '}';
    }

    private static boolean $default$enabled() {
        return true;
    }


    public static class PreferenceKeyBuilder {
        private Long id;
        private User user;
        private String keyhash;
        private boolean enabled$set;
        private boolean enabled$value;

        PreferenceKeyBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public PreferenceKey.PreferenceKeyBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PreferenceKey.PreferenceKeyBuilder user(final User user) {
            this.user = user;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PreferenceKey.PreferenceKeyBuilder keyhash(final String keyhash) {
            this.keyhash = keyhash;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PreferenceKey.PreferenceKeyBuilder enabled(final boolean enabled) {
            this.enabled$value = enabled;
            enabled$set = true;
            return this;
        }

        public PreferenceKey build() {
            boolean enabled$value = this.enabled$value;
            if (!this.enabled$set) enabled$value = PreferenceKey.$default$enabled();
            return new PreferenceKey(this.id, this.user, this.keyhash, enabled$value);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PreferenceKey.PreferenceKeyBuilder(id=" + this.id + ", user=" + this.user + ", keyhash=" + this.keyhash + ", enabled$value=" + this.enabled$value + ")";
        }
    }

    public static PreferenceKey.PreferenceKeyBuilder builder() {
        return new PreferenceKey.PreferenceKeyBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public String getKeyhash() {
        return this.keyhash;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setKeyhash(final String keyhash) {
        this.keyhash = keyhash;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public PreferenceKey() {
        this.enabled = PreferenceKey.$default$enabled();
    }

    protected PreferenceKey(final Long id, final User user, final String keyhash, final boolean enabled) {
        this.id = id;
        this.user = user;
        this.keyhash = keyhash;
        this.enabled = enabled;
    }
}
