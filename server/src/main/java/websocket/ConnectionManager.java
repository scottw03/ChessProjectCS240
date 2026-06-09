package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
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
        if (game != null) {
            game.remove(username);
        }
    }

    public void broadcastToGame(
            int gameID,
            Object message)
        throws IOException {
        Map<String, Connection> game =
                gameConnections.get(gameID);
        if (game == null) {
            return;
        }
        String json = gson.toJson(message);
        for (Connection connection : game.values()) {
            connection
                    .getSession()
                    .getRemote()
                    .sendString(json);
        }
    }

    public void broadcastExcept(
            int gameID,
            String excludedUser,
            Object message)
        throws IOException {
        Map<String, Connection> game =
                gameConnections.get(gameID);
        if (game == null) {
            return;
        }
        String json = gson.toJson(message);
        for (Connection connection : game.values()) {
            if (connection.getUsername()
                    .equals(excludedUser)) {
                continue;
            }
            connection
                    .getSession()
                    .getRemote()
                    .sendString(json);
        }
    }



}
