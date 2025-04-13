package org.barrikeit.controller;

import lombok.extern.log4j.Log4j2;
import org.barrikeit.service.SessionManager;
import org.barrikeit.service.dto.GameMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class GameController {
  private final SimpMessagingTemplate messagingTemplate;
  private final SessionManager sessionManager;

  public GameController(SimpMessagingTemplate messagingTemplate, SessionManager sessionManager) {
    this.messagingTemplate = messagingTemplate;
    this.sessionManager = sessionManager;
  }

  @MessageMapping("/game/{gameId}")
  @SendTo("/topic/game-room/{gameId}")
  public GameMessage handleMessage(
      @DestinationVariable String gameId, @Payload GameMessage message) {
    log.info("Message from user:{}", message.getUser());
    log.info("Board state :{}", message.getFen());
    log.info("Move sent :{}", message.getPgn());
    log.info("Sent message to game room: /topic/game-room/{}", gameId);
    return message;
  }
}
