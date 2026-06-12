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
            System.out.println("Session open = " + session.isOpen());
            String json = GSON.toJson(message);
            System.out.println("Sending message:");
            System.out.println(json.length() + " chars");
            session.getRemote().sendString(json);
            System.out.println("Send completed");
        }
    }

    public Session getSession() {
        return session;
    }
}
