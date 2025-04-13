package org.barrikeit.controller;

import lombok.extern.log4j.Log4j2;
import org.barrikeit.service.SessionManager;
import org.barrikeit.service.dto.BasicMessage;
import org.barrikeit.service.dto.connections.UserConnectMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class UserController {
  private final SessionManager sessionManager;

  public UserController(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @MessageMapping("/user/connect")
  public void connectUser(@Payload UserConnectMessage message) {
    sessionManager.connectUser(message.getUser());
  }

  @MessageMapping("/user/disconnect")
  public void disconnectUser(@Payload UserConnectMessage message) {
    sessionManager.disconnectUser(message.getUser());
  }

  @MessageMapping("/user/request-users")
  public void requestUsers() {
    log.info("Active users list requested");
    sessionManager.broadcastActiveUsers();
  }
}
