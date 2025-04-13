package org.barrikeit.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class SessionManager {
  private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
  private final Set<String> activeRooms = ConcurrentHashMap.newKeySet();
  private final ConcurrentMap<String, String> usersRooms = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Long> userLastActivity = new ConcurrentHashMap<>();
  private final SimpMessagingTemplate messagingTemplate;

  @Autowired
  public SessionManager(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void connectUser(String username) {
    activeUsers.add(username);
    log.info("User connected: {}", username);
    broadcastActiveUsers();
  }

  public void disconnectUser(String username) {
    activeUsers.remove(username);
    log.info("User disconnected: {}", username);
    broadcastActiveUsers();
  }

  public void broadcastActiveUsers() {
    log.info("Broadcasting active users to /topic/users {}", activeUsers);
    messagingTemplate.convertAndSend("/topic/users", activeUsers);
  }

  public void updateUserActivity(String username) {
    userLastActivity.put(username, System.currentTimeMillis());
  }

  @Scheduled(fixedRate = 60000)
  public void removeInactiveUsers() {
    long now = System.currentTimeMillis();
    long timeout = 300000; // 5 minutes
    for (Map.Entry<String, Long> entry : userLastActivity.entrySet()) {
      if (now - entry.getValue() > timeout) {
        String username = entry.getKey();
        leaveRoom(username);
        disconnectUser(username);
        userLastActivity.remove(username);
      }
    }
  }

  public void broadcastActiveRooms() {
    log.info("Broadcasting active rooms to /topic/rooms {}", activeRooms);
    messagingTemplate.convertAndSend("/topic/rooms", activeRooms);
  }

  public void joinRoom(String roomId, String username) {
    if (!activeUsers.contains(username)) {
      throw new IllegalStateException("User is not connected");
    }

    String existingRoom = usersRooms.putIfAbsent(username, roomId);
    if (existingRoom != null) {
      if (existingRoom.equals(roomId)) {
        throw new IllegalStateException("User is already in the room");
      } else {
        usersRooms.replace(username, roomId);
      }
    }

    activeRooms.add(roomId);
    log.info("User {} joined room {}", username, roomId);
    broadcastActiveRooms();
  }

  public void leaveRoom(String username) {
    String roomId = usersRooms.remove(username);
    if (roomId != null && !usersRooms.containsValue(roomId)) {
      activeRooms.remove(roomId);
    }
    log.info("User {} left their room", username);
    broadcastActiveRooms();
  }

  public List<String> getUsersInRoom(String roomId) {
    return usersRooms.entrySet().stream()
        .filter(entry -> roomId.equals(entry.getValue()))
        .map(Map.Entry::getKey)
        .toList();
  }
}
