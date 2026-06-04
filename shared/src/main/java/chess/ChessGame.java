package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(pos);
                if (piece != null) {
                    ChessPiece copiedPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    copiedPiece.setHasMoved(piece.hasMoved());
                    newBoard.addPiece(pos, copiedPiece);
                }
            }
        }
        return newBoard;
    }

    private boolean isCastleMove(
            ChessMove move, ChessPiece piece) {
        return piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(
                        move.getStartPosition().getColumn()
                - move.getEndPosition().getColumn()) == 2;
    }

    private ChessGame simulateMove(ChessMove move) {
        ChessBoard tempBoard = copyBoard(board);
        ChessPiece movingPiece =
                tempBoard.getPiece(
                        move.getStartPosition());
        tempBoard.addPiece(
                move.getEndPosition(),
                movingPiece);
        tempBoard.addPiece(
                move.getStartPosition(),
                null);
        ChessGame tempGame =
                new ChessGame();
        tempGame.setBoard(tempBoard);
        return tempGame;
    }

    private boolean castlePassesIntermediateCheck(
            ChessMove move,
            ChessPiece king) {
        if (!isCastleMove(move, king)) {
            return true;
        }
        if (isInCheck(king.getTeamColor())) {
            return false;
        }
        int row =
                move.getStartPosition().getRow();
        int middleCol =
                move.getEndPosition().getColumn() == 7
                        ? 6
                        : 4;
        ChessBoard middleBoard =
                copyBoard(board);
        ChessPosition middlePosition =
                new ChessPosition(
                        row,
                        middleCol);
        ChessPiece kingPiece =
                middleBoard.getPiece(
                        move.getStartPosition());

        middleBoard.addPiece(
                middlePosition,
                kingPiece);
        middleBoard.addPiece(
                move.getStartPosition(),
                null);
        ChessGame middleGame =
                new ChessGame();
        middleGame.setBoard(middleBoard);
        return !middleGame.isInCheck(
                king.getTeamColor());
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves =
                piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            if (!castlePassesIntermediateCheck(
                    move, piece)) {
                continue;
            }
            ChessGame simulatedGame =
                    simulateMove(move);
            if (!simulatedGame.isInCheck(
                    piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException();
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (!legalMoves.contains(move)) {
            throw new InvalidMoveException();
        }
        boolean castleMove = piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2;
        ChessPiece movedPiece = piece;
        if (move.getPromotionPiece() != null) {
            movedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        board.addPiece(move.getEndPosition(), movedPiece);
        board.addPiece(move.getStartPosition(), null);
        piece.setHasMoved(true);
        if (castleMove) {
            int row = move.getStartPosition().getRow();
            if (move.getEndPosition().getColumn() == 7) {
                ChessPosition rookStart = new ChessPosition(row, 8);
                ChessPosition rookEnd = new ChessPosition(row, 6);
                ChessPiece rook = board.getPiece(rookStart);
                board.addPiece(rookEnd, rook);
                board.addPiece(rookStart, null);
                rook.setHasMoved(true);
            }
            else if (move.getEndPosition().getColumn() == 3) {
                ChessPosition rookStart = new ChessPosition(row, 1);
                ChessPosition rookEnd = new ChessPosition(row, 4);
                ChessPiece rook = board.getPiece(rookStart);
                board.addPiece(rookEnd, rook);
                board.addPiece(rookStart, null);
                rook.setHasMoved(true);
            }
        }
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    private ChessPosition findKing(TeamColor color) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == color && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKing(teamColor);
        TeamColor enemy;
        if (teamColor == TeamColor.WHITE) {
            enemy = TeamColor.BLACK;
        } else {
            enemy = TeamColor.WHITE;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == enemy) {
                    Collection<ChessMove> enemyMoves = piece.pieceMoves(board, pos);
                    for (ChessMove move : enemyMoves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasLegalMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos =
                        new ChessPosition(row, col);
                ChessPiece piece =
                        board.getPiece(pos);
                if (piece == null ||
                        piece.getTeamColor() != teamColor) {
                    continue;
                }
                Collection<ChessMove> moves =
                        validMoves(pos);
                if (moves != null &&
                        !moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return !hasLegalMove(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return !hasLegalMove(teamColor);
    }
}