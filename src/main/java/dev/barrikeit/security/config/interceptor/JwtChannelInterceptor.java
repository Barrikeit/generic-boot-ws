package dev.barrikeit.security.config.interceptor;

import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * STEP 3 — JWT validation on the STOMP CONNECT frame.
 *
 * <p>This is the WS equivalent of the REST app's JwtFilter. It runs only once per connection (on
 * CONNECT), not on every message. After a successful CONNECT, the authenticated principal is stored
 * in the STOMP session and is available in every @MessageMapping via Principal.
 *
 * <p>Validation chain (same as REST app's JwtFilter): 1. Extract Bearer token from the STOMP
 * Authorization header 2. Parse and verify JWT signature + issuer (JwtUtil.parseToken throws on
 * failure) 3. Check the jti exists in user_sessions (DB revocation check) 4. Attach
 * UsernamePasswordAuthenticationToken as the STOMP session principal
 *
 * <p>Disabled entirely when security.enabled=false (local dev mode).
 */
@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtChannelInterceptor implements ChannelInterceptor {

  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    // Only intercept CONNECT — all other frames reuse the session principal
    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    log.debug("STOMP CONNECT — validating JWT");

    String jwt = extractToken(accessor);

    if (!StringUtils.hasText(jwt)) {
      log.warn("STOMP CONNECT rejected — no Authorization header");
      throw new MessagingException("Missing Authorization header");
    }

    try {
      // Step A: parse + verify signature and issuer
      String username = jwtUtil.extractUsername(jwt);

      // Step B: DB revocation check — same as REST app's validateActiveSession()
      validateActiveSession(jwt);

      // Step C: build authentication and attach to the STOMP session
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(username, null, jwtUtil.extractAuthorities(jwt));
      auth.setDetails(jwt);

      accessor.setUser(auth); // principal now available in @MessageMapping via Principal

      log.debug("STOMP CONNECT accepted — user: {}", username);

    } catch (ExpiredJwtException e) {
      log.warn("STOMP CONNECT rejected — JWT expired");
      throw new MessagingException("JWT Token expirado");
    } catch (SignatureException e) {
      log.warn("STOMP CONNECT rejected — invalid signature");
      throw new MessagingException("Token inválido");
    } catch (SessionAuthenticationException e) {
      log.warn("STOMP CONNECT rejected — session revoked");
      throw new MessagingException("Sesión no válida");
    }

    return message;
  }

  /**
   * Checks that the jti still exists in the user_sessions table. A missing row means the REST app
   * revoked this token.
   */
  private void validateActiveSession(String jwt) {
    UUID userCode = jwtUtil.extractUserCode(jwt);
    String jti = jwtUtil.extractJti(jwt);
    if (!userSessionService.validateToken(userCode, jti)) {
      throw new SessionAuthenticationException("Sesión no válida");
    }
  }

  /**
   * Extracts the raw JWT from the STOMP Authorization header. Clients must send: Authorization:
   * Bearer <token>
   */
  private String extractToken(StompHeaderAccessor accessor) {
    String header = accessor.getFirstNativeHeader("Authorization");
    return (StringUtils.hasText(header) && header.startsWith("Bearer "))
        ? header.substring(7)
        : null;
  }
}
