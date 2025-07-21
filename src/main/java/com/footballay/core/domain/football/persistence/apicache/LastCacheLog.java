package com.footballay.core.domain.football.persistence.apicache;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
public class LastCacheLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ApiCacheType apiCacheType;
    @Column(nullable = false, updatable = false)
    @Convert(converter = JsonFieldConverter.class)
    private Map<String, Object> parametersJson;
    @Column(nullable = false)
    private ZonedDateTime lastCachedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastCacheLog lastCacheLog = (LastCacheLog) o;
        if (getId() != null ? !getId().equals(lastCacheLog.getId()) : lastCacheLog.getId() != null) return false;
        if (getApiCacheType() != lastCacheLog.getApiCacheType()) return false;
        return getParametersJson() != null ? getParametersJson().equals(lastCacheLog.getParametersJson()) : lastCacheLog.getParametersJson() == null;
    }

    public boolean equalsWithTime(Object o) {
        return equals(o) && ((LastCacheLog) o).getLastCachedAt().equals(this.lastCachedAt);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getApiCacheType() != null ? getApiCacheType().hashCode() : 0);
        result = 31 * result + (getParametersJson() != null ? getParametersJson().hashCode() : 0);
        return result;
    }


    public static class LastCacheLogBuilder {
        private Long id;
        private ApiCacheType apiCacheType;
        private Map<String, Object> parametersJson;
        private ZonedDateTime lastCachedAt;

        LastCacheLogBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public LastCacheLog.LastCacheLogBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LastCacheLog.LastCacheLogBuilder apiCacheType(final ApiCacheType apiCacheType) {
            this.apiCacheType = apiCacheType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LastCacheLog.LastCacheLogBuilder parametersJson(final Map<String, Object> parametersJson) {
            this.parametersJson = parametersJson;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LastCacheLog.LastCacheLogBuilder lastCachedAt(final ZonedDateTime lastCachedAt) {
            this.lastCachedAt = lastCachedAt;
            return this;
        }

        public LastCacheLog build() {
            return new LastCacheLog(this.id, this.apiCacheType, this.parametersJson, this.lastCachedAt);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LastCacheLog.LastCacheLogBuilder(id=" + this.id + ", apiCacheType=" + this.apiCacheType + ", parametersJson=" + this.parametersJson + ", lastCachedAt=" + this.lastCachedAt + ")";
        }
    }

    public static LastCacheLog.LastCacheLogBuilder builder() {
        return new LastCacheLog.LastCacheLogBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public ApiCacheType getApiCacheType() {
        return this.apiCacheType;
    }

    public Map<String, Object> getParametersJson() {
        return this.parametersJson;
    }

    public ZonedDateTime getLastCachedAt() {
        return this.lastCachedAt;
    }

    public LastCacheLog() {
    }

    public LastCacheLog(final Long id, final ApiCacheType apiCacheType, final Map<String, Object> parametersJson, final ZonedDateTime lastCachedAt) {
        this.id = id;
        this.apiCacheType = apiCacheType;
        this.parametersJson = parametersJson;
        this.lastCachedAt = lastCachedAt;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "LastCacheLog(id=" + this.getId() + ", apiCacheType=" + this.getApiCacheType() + ", parametersJson=" + this.getParametersJson() + ", lastCachedAt=" + this.getLastCachedAt() + ")";
    }

    public void setLastCachedAt(final ZonedDateTime lastCachedAt) {
        this.lastCachedAt = lastCachedAt;
    }
}
