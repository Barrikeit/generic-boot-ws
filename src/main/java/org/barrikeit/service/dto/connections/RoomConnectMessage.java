package org.barrikeit.service.dto.connections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomConnectMessage {
  private String roomId;

  public RoomConnectMessage(String roomId) {
    this.roomId = roomId;
  }

  public RoomConnectMessage() {}
}
