package org.barrikeit.controller;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.barrikeit.service.SessionManager;
import org.barrikeit.service.dto.connections.RoomConnectMessage;
import org.barrikeit.service.dto.connections.UserConnectMessage;
import org.barrikeit.service.dto.connections.UserRoomConnectMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class RoomController {

  private final SessionManager sessionManager;

  public RoomController(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @MessageMapping("/room/join")
  public void joinRoom(@Payload UserRoomConnectMessage message) {
    sessionManager.joinRoom(message.getRoomId(), message.getUser());
    log.info("User {} joined room {}", message.getUser(), message.getRoomId());
  }

  @MessageMapping("/room/leave")
  public void leaveRoom(@Payload UserConnectMessage message) {
    sessionManager.leaveRoom(message.getUser());
  }

  @MessageMapping("/room/request-rooms")
  public void requestRooms() {
    log.info("Active rooms list requested");
    sessionManager.broadcastActiveRooms();
  }

  @MessageMapping("/room/users")
  @SendTo("/topic/room-users")
  public List<String> getUsersInRoom(@Payload RoomConnectMessage message) {
    List<String> users = sessionManager.getUsersInRoom(message.getRoomId());
    log.info("Users in room {}: {}", message.getRoomId(), users);
    return users;
  }
}
