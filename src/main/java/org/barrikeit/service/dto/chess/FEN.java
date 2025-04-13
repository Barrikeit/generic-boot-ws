package org.barrikeit.service.dto.chess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;

@Builder
public record FEN(
    ChessBoard chessBoard,
    Turn turn,
    String castlingAvailability,
    String enpassantTarget,
    int halfmoveClock,
    int fullmoveNumber) {

  public static FEN start() {
    return FEN.fromString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
  }

  @JsonCreator
  public static FEN fromString(String fenString) {
    String[] parts = fenString.split(" ");
    if (parts.length != 6) {
      throw new IllegalArgumentException("Invalid FEN string: " + fenString);
    }
    ChessBoard board = ChessBoard.fromFEN(parts[0]);
    Turn turn = parts[1].equals("w") ? Turn.WHITE : Turn.BLACK;
    String castling = parts[2];
    String enpassant = parts[3];
    int halfmove = Integer.parseInt(parts[4]);
    int fullmove = Integer.parseInt(parts[5]);

    return FEN.builder()
        .chessBoard(board)
        .turn(turn)
        .castlingAvailability(castling)
        .enpassantTarget(enpassant)
        .halfmoveClock(halfmove)
        .fullmoveNumber(fullmove)
        .build();
  }

  @JsonValue
  public String toString() {
    return chessBoard
        + " "
        + turn.getValue()
        + " "
        + castlingAvailability
        + " "
        + enpassantTarget
        + " "
        + halfmoveClock
        + " "
        + fullmoveNumber;
  }
}
