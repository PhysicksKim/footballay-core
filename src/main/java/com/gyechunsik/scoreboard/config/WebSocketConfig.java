package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.websocket.test.CustomHandshakeHandler;
import com.gyechunsik.scoreboard.websocket.test.HttpHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * Allow CORS origin 값을 "${cors.allowedorigins}" 환경변수에서 가져옵니다.
 * dev , release 환경에 따라서 cors 설정을 다르게 해줘야 하며,
 * 실행 환경 변수로 active profile 을 다르게 설정해줌으로써 cors allow origin 값을 다르게 설정합니다.
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Map<String, String> sessionKeys = new ConcurrentHashMap<>();
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
        registry.addEndpoint("/ws").setAllowedOrigins("*")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .addInterceptors(new HttpHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로 메시지를 라우팅할 때 사용할 prefix를 설정합니다.
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
