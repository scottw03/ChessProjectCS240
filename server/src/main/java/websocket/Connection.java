package websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import org.eclipse.jetty.websocket.api.Session;

public class Connection {
    private final String username;
    private final Session session;

    private static final Gson GSON =
            new Gson();

    public Connection(String username,
                      Session session) {
        this.username = username;
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public void send(
            ServerMessage message)
        throws Exception {
        if (session.isOpen()) {
            session.getRemote().sendString(
                    GSON.toJson(message));
        }
    }

    public Session getSession() {
        return session;
    }
}
