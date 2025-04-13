package org.barrikeit.service.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WSMessage<M extends BasicMessage> {
  private MessageAction action;
  private ChannelType type;
  private String channel;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessage.class, name = "CHAT"),
    @JsonSubTypes.Type(value = GameMessage.class, name = "GAME")
  })
  private M message;

  public String toString() {
    return "{\"action\":\""
        + this.getAction()
        + "\",\"type\":\""
        + this.getType()
        + "\",\"channel\":\""
        + this.getChannel()
        + "\",\"message\":"
        + this.getMessage()
        + "}";
  }
}
