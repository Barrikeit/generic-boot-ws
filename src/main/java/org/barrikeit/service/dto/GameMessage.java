package org.barrikeit.service.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.barrikeit.service.dto.chess.FEN;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameMessage extends BasicMessage {
  private String pgn;
  private FEN fen;

  public GameMessage(String user, String pgn, FEN fen) {
    super(user);
    this.pgn = pgn;
    this.fen = fen;
  }

  public String toString() {
    return "{"
        + "\"user\":\""
        + this.getUser()
        + "\",\"pgn\":\""
        + this.getPgn()
        + "\",\"fen\":\""
        + this.getFen()
        + "\"}";
  }
}
