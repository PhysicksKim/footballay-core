package com.gyechunsik.scoreboard.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link IncludeDeleted} 어노테이션을 사용하면 soft delete 된 엔티티를 포함하여 조회할 수 있습니다.
 * @see HibernateFilterAspect
 * @see HibernateConfig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IncludeDeleted {
}
