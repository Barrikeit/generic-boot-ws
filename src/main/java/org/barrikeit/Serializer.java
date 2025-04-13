package org.barrikeit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.barrikeit.service.dto.GameMessage;
import org.barrikeit.service.dto.WSMessage;
import org.barrikeit.service.dto.chess.FEN;

public class Serializer {
  public static void main(String[] args) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    // Create GameMessage object
    GameMessage msg = new GameMessage("helloWorld", "e2", FEN.start());

    // Serialize object to JSON
    String json = objectMapper.writeValueAsString(msg);
    System.out.println("Serialized JSON: ");
    System.out.println(json);

    // Deserialize JSON back to GameMessage
    GameMessage deserializedMove = objectMapper.readValue(json, GameMessage.class);
    System.out.println("Deserialized Object: ");
    System.out.println(deserializedMove);

    // Test with full WebSocket message
    String json2 =
        "{ \n"
            + "  \"action\": \"SEND\",\n"
            + "  \"type\": \"GAME\",\n"
            + "  \"channel\": \"room-1\",\n"
            + "  \"message\": {\n"
            + "    \"user\": \"helloWorld\",\n"
            + "    \"pgn\": \"e2\",\n"
            + "    \"fen\": \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\"\n"
            + "  }\n"
            + "}\n";
    // Deserialize the full JSON
    WSMessage<GameMessage> wsMessage = objectMapper.readValue(json2, WSMessage.class);
    System.out.println("Deserialized Message: ");
    System.out.println(wsMessage);
  }
}
