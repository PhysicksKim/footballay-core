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
 * soft delete 된 엔티티를 포함해서 조회하고 싶은 경우 사용합니다.
 * @see IncludeDeleted
 */
@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class HibernateFilterAspect {

    private final SessionFactory sessionFactory;

    private static final String SOFT_DELETE_FILTER_NAME = "SoftDeleteFilter";

    /**
     * 엔티티 조회 전 필터 비활성화
     */
    @Before("@annotation(IncludeDeleted)")
    public void disableSoftDeleteFilter(JoinPoint joinPoint) {
        Session session = sessionFactory.getCurrentSession();
        session.disableFilter(SOFT_DELETE_FILTER_NAME);
    }

    /**
     * 엔티티 조회 후 필터 재활성화
     */
    @After("@annotation(IncludeDeleted)")
    public void restoreSoftDeleteFilter(JoinPoint joinPoint) {
        Session session = sessionFactory.getCurrentSession();
        Filter filter = session.enableFilter(SOFT_DELETE_FILTER_NAME);
        filter.setParameter("softDeleteBool", true);
        filter.validate();
    }
}
