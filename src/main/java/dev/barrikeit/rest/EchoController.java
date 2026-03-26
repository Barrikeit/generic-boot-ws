package dev.barrikeit.rest;

import java.security.Principal;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * Minimal STOMP controller to verify the full pipeline works.
 *
 * <p>Two endpoints:
 *
 * <ul>
 *   <li>/app/echo → /topic/echo — broadcasts to all subscribers (needs auth)
 *   <li>/app/ping → /user/queue/pong — replies only to the sender (needs auth)
 * </ul>
 *
 * <p>Principal is injected automatically by Spring from the STOMP session. It is the
 * UsernamePasswordAuthenticationToken set by JwtChannelInterceptor. principal.getName() returns the
 * username extracted from the JWT subject claim.
 */
@Log4j2
@Controller
public class EchoController {

  /**
   * Broadcast echo — any authenticated client can send here. Client sends to: /app/echo All
   * subscribers receive on: /topic/echo
   */
  @MessageMapping("/echo")
  @SendTo("/topic/echo")
  public EchoMessage echo(@Payload String text, Principal principal) {
    log.debug("Echo from user [{}]: {}", principal.getName(), text);
    return new EchoMessage(principal.getName(), text);
  }

  /**
   * Private ping — reply goes only to the sender. Client sends to: /app/ping Sender receives on:
   * /user/queue/pong
   */
  @MessageMapping("/ping")
  @SendToUser("/queue/pong")
  public EchoMessage ping(Principal principal) {
    log.debug("Ping from user [{}]", principal.getName());
    return new EchoMessage(principal.getName(), "pong");
  }

  public record EchoMessage(String sender, String content) {}

  @MessageMapping("/hello")
  @SendTo("/topic/hello")
  public EchoMessage hello(@Payload HelloMessage payload, Principal principal) {
    log.debug("Hello from [{}]: {}", principal.getName(), payload.name());
    return new EchoMessage(principal.getName(), "Hello " + payload.name());
  }

  public record HelloMessage(String name) {}
}
