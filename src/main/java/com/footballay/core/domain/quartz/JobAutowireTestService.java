package com.footballay.core.domain.quartz;

import org.springframework.stereotype.Service;

@Service
public class JobAutowireTestService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobAutowireTestService.class);

    public void test() {
        log.info("JobAutowireTestService test called");
    }
}
