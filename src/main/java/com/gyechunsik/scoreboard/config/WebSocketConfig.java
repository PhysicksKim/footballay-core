package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.websocket.handler.CustomHandshakeHandler;
import com.gyechunsik.scoreboard.websocket.handler.HttpHandshakeInterceptor;
import com.gyechunsik.scoreboard.websocket.handler.StompChannelInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
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

    private final StompChannelInterceptor stompHandler;

    /**
     * stompHandler 내부의 빈 순환 의존성 문제로 인해서 @Lazy 로 설정합니다.
     * @param stompHandler
     */
    public WebSocketConfig(@Lazy StompChannelInterceptor stompHandler) {
        this.stompHandler = stompHandler;
    }

    /*
     yml 파일에서는 List<String> 으로 받아올 수 없는 버그가 있습니다
     그래서 단일 String 으로 "url1,url2,url3" 이런식으로 받아서 split 으로 처리합니다.
     */
    @Value("${cors.allowedorigins}")
    private String rawAllowedOrigins;

    /**
     * allow origins 을 명시하지 않고 * 로 설정하면 에러가 발생합니다.
     * 이는 allowCredentials 가 true 일 때 * 로 설정할 수 없는 정책 때문입니다.
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowOriginArray = rawAllowedOrigins.split(",");
        log.info("allow origins : {}", Arrays.toString(allowOriginArray));
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowOriginArray)
                .setHandshakeHandler(new CustomHandshakeHandler())
                .addInterceptors(new HttpHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로 메시지를 라우팅할 때 사용할 prefix를 설정합니다.
        registry.enableSimpleBroker("/topic","/queue");
        registry.setApplicationDestinationPrefixes("/app","/chat");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }

}
