package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Gson gson = new Gson();
    private final Map<Integer,
            Map<String, Connection>> gameConnections =
            new ConcurrentHashMap<>();

    public void addConnection(
            int gameID,
            String username,
            Session session) {
        System.out.println(
                username + " connected to game "
                + gameID);
        gameConnections.computeIfAbsent(
                gameID,
                id -> new ConcurrentHashMap<>()).put(
                        username,
                new Connection(
                        username,
                        session));
    }

    public void removeConnection(
            int gameID,
            String username) {
        Map<String, Connection> game =
                gameConnections.get(gameID);
        if (game == null) {
            return;
        }
        game.remove(username);
        if (game.isEmpty()) {
            gameConnections.remove(gameID);
        }
    }

    public void sendToUser(
            int gameID,
            String username,
            ServerMessage message)
        throws Exception {
        Map<String, Connection> gameMap =
                gameConnections.get(gameID);
        if (gameMap == null) {
            return;
        }
        Connection connection =
                gameMap.get(username);
        if (connection == null) {
            return;
        }
        connection.send(message);
    }

    public void broadcast(
            int gameID,
            ServerMessage message)
        throws Exception {
        Map<String, Connection> game =
                gameConnections.get(gameID);
        if (game == null) {
            return;
        }
        for (Connection connection : game.values()) {
            connection.send(message);
        }
    }

    public void broadcastExcept(
            int gameID,
            String excludedUser,
            ServerMessage message)
        throws Exception {
        Map<String, Connection> game =
                gameConnections.get(gameID);
        if (game == null) {
            return;
        }
        for (Connection connection :
        game.values()) {
            if (connection.getUsername()
                    .equals(excludedUser)) {
                continue;
            }
            connection.send(message);
        }
    }
}
