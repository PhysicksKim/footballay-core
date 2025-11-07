package com.footballay.core.web.common.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class IndexPageControllerTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IndexPageControllerTest.class);
    @Autowired
    private IndexPageController indexPageController;

    @DisplayName("footballay 메인 페이지를 가져옵니다")
    @Test
    void successGetFootballayMainPage() {
        // when
        ResponseEntity<String> stringResponseEntity = indexPageController.footballayIndexPage();
        // then
        assertEquals(HttpStatusCode.valueOf(200), stringResponseEntity.getStatusCode());
        assertNotNull(stringResponseEntity.getBody());
    }
}
