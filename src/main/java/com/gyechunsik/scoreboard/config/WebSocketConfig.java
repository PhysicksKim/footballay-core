package com.gyechunsik.scoreboard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

/**
 * <pre>
 * Allow CORS origin 값을 "${cors.allowedorigins}" 환경변수에서 가져옵니다.
 * dev , release 환경에 따라서 cors 설정을 다르게 해줘야 하며,
 * 실행 환경 변수로 active profile 을 다르게 설정해줌으로써 cors allow origin 값을 다르게 설정합니다.
 * </pre>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private Environment env;

    /*
     yml 파일에서는 List<String> 으로 받아올 수 없는 버그가 있습니다
     그래서 단일 String 으로 "url1,url2,url3" 이런식으로 받아서 split 으로 처리합니다.
     */
    @Value("${cors.allowedorigins}")
    private String rawAllowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowOriginArray = rawAllowedOrigins.split(",");
        log.info("allow origins : {}", Arrays.toString(allowOriginArray));
        // registry.addEndpoint("/ws").setAllowedOrigins(allowOriginArray);
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 클라이언트로 메시지를 라우팅할 때 사용할 prefix를 설정합니다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}
