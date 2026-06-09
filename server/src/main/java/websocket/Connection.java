package websocket;

import org.eclipse.jetty.websocket.api.Session;

public class Connection {
    private final String username;
    private final Session session;

    public Connection(String username,
                      Session session) {
        this.username = username;
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public Session getSession() {
        return session;
    }
}
