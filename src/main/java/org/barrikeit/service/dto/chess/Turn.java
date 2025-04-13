package org.barrikeit.service.dto.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Turn {
    WHITE("w"),
    BLACK("b");

    private final String value;
}
