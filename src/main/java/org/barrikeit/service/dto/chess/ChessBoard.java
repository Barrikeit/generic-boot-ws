package org.barrikeit.service.dto.chess;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;

@Builder
public record ChessBoard(List<String> rows) {

  public static ChessBoard start() {
    return ChessBoard.builder()
        .rows(List.of("rnbqkbnr", "pppppppp", "8", "8", "8", "8", "PPPPPPPP", "RNBQKBNR"))
        .build();
  }

  @JsonCreator
  public static ChessBoard fromFEN(String fen) {
    return ChessBoard.builder().rows(List.of(fen.split("/"))).build();
  }

  @JsonValue
  public String toString() {
    return String.join("/", rows);
  }
}
