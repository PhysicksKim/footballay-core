package com.gyechunsik.scoreboard.domain.football.entity.apicache;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    @Setter
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
}
