package org.barrikeit.controller;

import lombok.extern.log4j.Log4j2;
import org.barrikeit.service.SessionManager;
import org.barrikeit.service.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class ChatController {
  private final SimpMessagingTemplate messagingTemplate;
  private final SessionManager sessionManager;

  public ChatController(SimpMessagingTemplate messagingTemplate, SessionManager sessionManager) {
    this.messagingTemplate = messagingTemplate;
    this.sessionManager = sessionManager;
  }

  @MessageMapping("/chat/{chatId}")
  @SendTo("/topic/chat-room/{chatId}")
  public ChatMessage handleMessage(
      @DestinationVariable String chatId, @Payload ChatMessage message) {
    log.info("Message from user:{}", message.getUser());
    log.info("Message sent :{}", message.getMessage());
    log.info("Sent message to chat room: /topic/chat-room/{}", chatId);
    return message;
  }
}
