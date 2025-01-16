package com.gyechunsik.scoreboard.entity;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class HibernateConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@link PreferenceKey} 에서 soft delete 를 위해 사용하는 필터 입니다.
     */
    private static final String SOFT_DELETE_FILTER_NAME = "enabledFilter";

    /**
     * soft delete 를 위한 필터를 활성화 합니다.
     */
    @PostConstruct
    @Transactional
    public void init() {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(SOFT_DELETE_FILTER_NAME);
        filter.setParameter("enabled", true);
        filter.validate();
    }
}
