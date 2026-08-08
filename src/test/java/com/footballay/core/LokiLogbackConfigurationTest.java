package com.footballay.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.loki4j.logback.Loki4jAppender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

class LokiLogbackConfigurationTest {

    private LoggerContext context;

    @BeforeEach
    void setUp() {
        context = new LoggerContext();
    }

    @AfterEach
    void tearDown() {
        context.stop();
    }

    @Test
    void productionLokiConfigurationUsesLoki4jOnePointFourProperties() throws Exception {
        var configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new ByteArrayInputStream(configuration().getBytes(StandardCharsets.UTF_8)));

        assertInstanceOf(Loki4jAppender.class, context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("LOKI"));
        assertFalse(
            context.getStatusManager().getCopyOfStatusList().stream()
                .map(status -> status.getMessage())
                .anyMatch(message -> message.contains("Ignoring unknown property") || message.contains("No encoder specified"))
        );
    }

    private String configuration() {
        return """
            <configuration>
                <property name="lokiUrl" value="http://localhost:3100/loki/api/v1/push"/>
                <property name="lokiUsername" value="test-user"/>
                <property name="lokiPassword" value="test-password"/>
                <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
                    <encoder><pattern>%msg%n</pattern></encoder>
                </appender>
                <include resource="logback-prod.xml"/>
            </configuration>
            """;
    }
}
