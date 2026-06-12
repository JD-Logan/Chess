package server;

import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // take sessionID to connection info


    private final ConcurrentHashMap<String, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<String>> gameSessions = new ConcurrentHashMap<>();

    record ConnectionInfo(String username, int gameID, WsContext ctx) {}

    public void add(String sessionID, String username, int gameID, WsContext ctx) {
        connections.put(sessionID, new ConnectionInfo(username, gameID, ctx));
        gameSessions
                .computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet())
                .add(sessionID);
    }

    public void remove(String sessionID) {
        ConnectionInfo info = connections.remove(sessionID);
        if (info == null) {
            return;
        }

        Set<String> sessions = gameSessions.get(info.gameID());
        if (sessions != null) {
            sessions.remove(sessionID);
            if (sessions.isEmpty()) {
                gameSessions.remove(info.gameID());
            }
        }
    }

    public void send(String sessionId, String json) {
        ConnectionInfo info = connections.get(sessionId);
        if (info != null) info.ctx().send(json);
    }

    public void broadcastToGame(int gameID, String json) {
        Set<String> sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return;
        }
        for (String sessionID : sessions) {
            send(sessionID, json);
        }
    }

    public void broadcastToGameExcept(int gameID, String excludeSessionID, String json) {
        Set<String> sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return;
        }
        for (String sessionID : sessions) {
            if (!sessionID.equals(excludeSessionID)) {
                send(sessionID, json);
            }
        }
    }

}
