package org.barrikeit.service.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessage extends BasicMessage {
  private String message;

  public ChatMessage(String user, String message) {
    super(user);
    this.message = message;
  }

  public String toString() {
    return "{\"user\":\"" + this.getUser() + "\",\"message\": \"" + this.getMessage() + "\"}";
  }
}
