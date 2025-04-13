package org.barrikeit.service.dto.connections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoomConnectMessage {
  private String user;
  private String roomId;

  public UserRoomConnectMessage(String user, String roomId) {
    this.user = user;
    this.roomId = roomId;
  }

  public UserRoomConnectMessage() {}
}
