package com.footballay.core.domain.football.persistence.live;

import jakarta.persistence.*;

@Entity
public class ExpectedGoals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_statistics_id", nullable = false)
    private TeamStatistics teamStatistics;
    /**
     * xG 값이 기록된 시간
     */
    private Integer elapsed;
    /**
     * xG 값 <br>
     * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
     * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
     * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
     */
    private String xg;

    @Override
    public String toString() {
        return "ExpectedGoals{" + "id=" + id + ", teamStatistics id =" + teamStatistics.getId() + ", elapsed=" + elapsed + ", xg=\'" + xg + '\'' + '}';
    }


    public static class ExpectedGoalsBuilder {
        private Long id;
        private TeamStatistics teamStatistics;
        private Integer elapsed;
        private String xg;

        ExpectedGoalsBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ExpectedGoals.ExpectedGoalsBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ExpectedGoals.ExpectedGoalsBuilder teamStatistics(final TeamStatistics teamStatistics) {
            this.teamStatistics = teamStatistics;
            return this;
        }

        /**
         * xG 값이 기록된 시간
         * @return {@code this}.
         */
        public ExpectedGoals.ExpectedGoalsBuilder elapsed(final Integer elapsed) {
            this.elapsed = elapsed;
            return this;
        }

        /**
         * xG 값 <br>
         * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
         * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
         * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
         * @return {@code this}.
         */
        public ExpectedGoals.ExpectedGoalsBuilder xg(final String xg) {
            this.xg = xg;
            return this;
        }

        public ExpectedGoals build() {
            return new ExpectedGoals(this.id, this.teamStatistics, this.elapsed, this.xg);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExpectedGoals.ExpectedGoalsBuilder(id=" + this.id + ", teamStatistics=" + this.teamStatistics + ", elapsed=" + this.elapsed + ", xg=" + this.xg + ")";
        }
    }

    public static ExpectedGoals.ExpectedGoalsBuilder builder() {
        return new ExpectedGoals.ExpectedGoalsBuilder();
    }

    public ExpectedGoals() {
    }

    /**
     * Creates a new {@code ExpectedGoals} instance.
     *
     * @param id
     * @param teamStatistics
     * @param elapsed xG 값이 기록된 시간
     * @param xg xG 값 <br>
     * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
     * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
     * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
     */
    public ExpectedGoals(final Long id, final TeamStatistics teamStatistics, final Integer elapsed, final String xg) {
        this.id = id;
        this.teamStatistics = teamStatistics;
        this.elapsed = elapsed;
        this.xg = xg;
    }

    public Long getId() {
        return this.id;
    }

    public TeamStatistics getTeamStatistics() {
        return this.teamStatistics;
    }

    /**
     * xG 값이 기록된 시간
     */
    public Integer getElapsed() {
        return this.elapsed;
    }

    /**
     * xG 값 <br>
     * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
     * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
     * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
     */
    public String getXg() {
        return this.xg;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setTeamStatistics(final TeamStatistics teamStatistics) {
        this.teamStatistics = teamStatistics;
    }

    /**
     * xG 값이 기록된 시간
     */
    public void setElapsed(final Integer elapsed) {
        this.elapsed = elapsed;
    }

    /**
     * xG 값 <br>
     * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
     * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
     * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
     */
    public void setXg(final String xg) {
        this.xg = xg;
    }
}
