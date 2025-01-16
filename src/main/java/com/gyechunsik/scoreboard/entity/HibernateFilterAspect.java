package com.gyechunsik.scoreboard.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

/**
 * Soft delete 를 위해 {@link IncludeDeleted} 를 사용하기 위해서 필터를 활성화/비활성화 하는 Aspect 입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class HibernateFilterAspect {

    private final SessionFactory sessionFactory;

    /**
     * {@link IncludeDeleted} 사용 시 soft delete 를 포함하여 엔티티를 조회하기 위해 {@link Before} 시점에 필터를 비활성화 합니다.
     * @param joinPoint
     */
    @Before("@annotation(IncludeDeleted)")
    public void disableEnabledFilter(JoinPoint joinPoint) {
        Session session = sessionFactory.getCurrentSession();
        session.disableFilter("enabledFilter");
    }

    /**
     * {@link IncludeDeleted} 사용 시 soft delete 를 포함하여 엔티티를 조회한 후에 필터를 다시 활성화 합니다.
     * @param joinPoint
     */
    @After("@annotation(IncludeDeleted)")
    public void restoreEnabledFilter(JoinPoint joinPoint) {
        Session session = sessionFactory.getCurrentSession();
        Filter filter = session.enableFilter("enabledFilter");
        filter.setParameter("enabled", true);
    }
}
