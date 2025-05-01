package com.footballay.core.domain.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobAutowireTestService {

    public void test() {
        log.info("JobAutowireTestService test called");
    }
}
