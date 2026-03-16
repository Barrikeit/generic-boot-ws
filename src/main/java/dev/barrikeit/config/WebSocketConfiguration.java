package dev.barrikeit.config;

import dev.barrikeit.security.config.interceptor.AppHeaderValidatorInterceptor;
import dev.barrikeit.security.config.interceptor.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STEP 1 — bare STOMP WebSocket setup.
 *
 * <p>Registers the /ws SockJS endpoint and the simple in-memory broker. Security interceptors
 * (Steps 2-3) are wired in configureClientInboundChannel.
 *
 * <p>Broker destinations:
 *
 * <ul>
 *   <li>/topic/** — broadcast (one → many)
 *   <li>/queue/** — point-to-point (one → one)
 * </ul>
 *
 * <p>Client sends to /app/**, which is routed to @MessageMapping methods.
 */
@Log4j2
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

  // STEP 3 — injected once security is added
  private final AppHeaderValidatorInterceptor appHeaderValidatorInterceptor;
  private final JwtChannelInterceptor jwtChannelInterceptor;

  /**
   * STEP 1 — register the STOMP endpoint. SockJS fallback is enabled for browsers that don't
   * support native WS. setAllowedOriginPatterns("*") — restrict this in production.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
  }

  /**
   * STEP 1 — configure the message broker. Simple in-memory broker; swap for RabbitMQ/ActiveMQ when
   * you need persistence.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setApplicationDestinationPrefixes("/app");
    // optional: user-specific destinations (e.g. /user/queue/errors)
    registry.setUserDestinationPrefix("/user");
  }

  /**
   * STEP 3 — wire channel interceptors in order: 1. AppHeaderValidatorInterceptor — validates the
   * custom app header on CONNECT 2. JwtChannelInterceptor — validates the Bearer JWT on CONNECT
   *
   * <p>Both run only on the CONNECT frame; subsequent frames reuse the established principal.
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(appHeaderValidatorInterceptor, jwtChannelInterceptor);
  }
}
