package org.barrikeit.service.dto.connections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserConnectMessage {
  private String user;

  public UserConnectMessage(String user) {
    this.user = user;
  }

  public UserConnectMessage() {}
}
