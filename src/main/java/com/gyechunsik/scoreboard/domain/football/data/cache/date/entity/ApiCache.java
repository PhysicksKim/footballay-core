package com.gyechunsik.scoreboard.domain.football.data.cache.date.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class ApiCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ApiCacheType apiCacheType;

    @Column(nullable = false, updatable = false)
    @Convert(converter = JsonFieldConverter.class)
    private Map<String, Object> parametersJson;

    @Setter
    @Column(nullable = false)
    private LocalDateTime lastCachedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiCache apiCache = (ApiCache) o;

        if (getId() != null ? !getId().equals(apiCache.getId()) : apiCache.getId() != null) return false;
        if (getApiCacheType() != apiCache.getApiCacheType()) return false;
        return getParametersJson() != null ? getParametersJson().equals(apiCache.getParametersJson()) : apiCache.getParametersJson() == null;
    }

    public boolean equalsWithTime(Object o) {
        return equals(o) && ((ApiCache) o).getLastCachedAt().equals(this.lastCachedAt);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getApiCacheType() != null ? getApiCacheType().hashCode() : 0);
        result = 31 * result + (getParametersJson() != null ? getParametersJson().hashCode() : 0);
        return result;
    }
}
