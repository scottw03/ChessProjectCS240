package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;
    @Override
    public int createGame(GameData game) throws DataAccessException {
        int gameID = nextGameID++;
        GameData newGame = new GameData(
                gameID,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );
        games.put(gameID, newGame);
        return gameID;
    }
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("bad request");
        }
        games.put(game.gameID(), game);
    }
    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextGameID = 1;
    }
}
