package com.footballay.core.web.admin.football.service;

import com.footballay.core.config.AppEnvironmentVariable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class AdminPageServiceTest {

    @MockBean
    private AppEnvironmentVariable envVar;

    @Autowired
    private AdminPageService adminPageService;

    @BeforeEach
    void setUp() {
        when(envVar.getFOOTBALLAY_STATIC_DOMAIN())
                .thenReturn("static.footballay.com");
    }

    @DisplayName("Admin page URI test")
    @Test
    void testGetAdminPageUri() {
        String expectedUri = "https://static.footballay.com/footballay/admin/index.html";
        String actualUri = adminPageService.getAdminPageUri();

        assertEquals(expectedUri, actualUri);
        log.info("Admin page URI: {}", actualUri);
    }


}