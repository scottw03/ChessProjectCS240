package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import service.requests.*;
import service.results.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;

public class ServerFacade {

    public AuthData register(String username,
                             String password,
                             String email) throws Exception {

    }

    public AuthData login(String username,
                          String password) throws Exception {

    }

    public void logout(String authToken) throws Exception {

    }

    public CreateGameResult createGame(String authToken,
                                       String gameName) throws Exception {

    }

    public ListGamesResult listGames(String authToken) throws Exception {

    }

    public void joinGame(String authToken,
                         String color,
                         int gameID) throws Exception {

    }

    public void clear() throws Exception {

    }


}
