package com.gyechunsik.scoreboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;

@Configuration
public class QuartzConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpringBeanJobFactory springBeanJobFactory;

    // TODO : Bean 으로 직접 넣으면 안됩니다. 이렇게 하면 yml 에서 설정한 값 대신 모두 기본값인 factory 가 덮어씌워집니다.
    // 예를 들어 thread count 를 5로 설정했으나 기본값인 10으로 들어가게 됩니다.
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(springBeanJobFactory); // SpringBeanJobFactory 설정
        return factory;
    }

}
