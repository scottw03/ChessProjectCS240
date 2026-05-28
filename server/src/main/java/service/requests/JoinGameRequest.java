package service.requests;

public record JoinGameRequest(
        String playerColor, Integer gameID, String authToken
) {
}
