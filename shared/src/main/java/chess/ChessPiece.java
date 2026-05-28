package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean hasMoved = false;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        hasMoved = moved;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    public boolean viableDestination(ChessBoard board, ChessPosition endPosition) {
        int row = endPosition.getRow();
        int col = endPosition.getColumn();
        if (row < 1 || row > 8 || col < 1 || col > 8){
            return false;
        }
        ChessPiece target = board.getPiece(endPosition);
        if (target == null) {
            return true;
        }
        else {
            return target.getTeamColor() != this.pieceColor;
        }
    }


    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        Collection<ChessMove> moves = new ArrayList<>();
        if (piece.getPieceType() == PieceType.KING) {

            int[][] directions = {
                    {1, 1}, {1, 0}, {1, -1},
                    {0, 1}, {0, -1},
                    {-1, 1}, {-1, 0}, {-1, -1}
            };
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();
                row += dir[0];
                col += dir[1];
                ChessPosition newPos = new ChessPosition(row, col);
                if (!viableDestination(board, newPos)) {
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPos, null));
            }
            if (!this.hasMoved()) {
                int row = myPosition.getRow();
                ChessPosition rookPos = new ChessPosition(row, 8);
                ChessPiece rook = board.getPiece(rookPos);
                if (rook != null && rook.getPieceType() == PieceType.ROOK && !rook.hasMoved()) {
                    ChessPosition f = new ChessPosition(row, 6);
                    ChessPosition g = new ChessPosition(row, 7);
                    if (board.getPiece(f) == null && board.getPiece(g) == null) {
                        moves.add(new ChessMove(myPosition, g, null
                        ));
                    }
                }
                rookPos = new ChessPosition(row, 1);
                rook = board.getPiece(rookPos);
                if (rook != null && rook.getPieceType() == PieceType.ROOK && !rook.hasMoved()) {
                    ChessPosition b = new ChessPosition(row, 2);
                    ChessPosition c = new ChessPosition(row, 3);
                    ChessPosition d = new ChessPosition(row, 4);
                    if (board.getPiece(b) == null && board.getPiece(c) == null && board.getPiece(d) == null) {
                        moves.add(new ChessMove(myPosition, c, null));
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.QUEEN) {
            int[][] directions = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}
            };
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();
                while (true){
                    row += dir[0];
                    col += dir[1];
                    ChessPosition newPos = new ChessPosition(row, col);
                    if (!viableDestination(board, newPos)) {
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPos, null));
                    if (board.getPiece(newPos) != null) {
                        break;
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.ROOK) {
            int[][] directions = {
                    {1, 0}, {0, 1}, {-1, 0}, {0, -1}
            };
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();
                while (true){
                    row += dir[0];
                    col += dir[1];
                    ChessPosition newPos = new ChessPosition(row, col);
                    if (!viableDestination(board, newPos)) {
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPos, null));
                    if (board.getPiece(newPos) != null) {
                        break;
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.BISHOP) {
            int[][] directions = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            };
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();
                while (true){
                    row += dir[0];
                    col += dir[1];
                    ChessPosition newPos = new ChessPosition(row, col);
                    if (!viableDestination(board, newPos)) {
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPos, null));
                    if (board.getPiece(newPos) != null) {
                        break;
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.KNIGHT) {
            int[][] directions = {
                    {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2,1}, {2, -1}, {-2, 1}, {-2, -1}
            };
            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();
                row += dir[0];
                col += dir[1];
                ChessPosition newPos = new ChessPosition(row, col);
                if (!viableDestination(board, newPos)) {
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }

        if (piece.getPieceType() == PieceType.PAWN) {
            int direction;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1;
            } else {
                direction = -1;
            }
            int startRow;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                startRow = 2;
            } else {
                startRow = 7;
            }
            int promotionRow;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                promotionRow = 8;
            }
            else {
                promotionRow = 1;
            }
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            int newRow = row + direction;
            ChessPosition oneForward = new ChessPosition(newRow, col);
            if (newRow >= 1 && newRow <= 8 && board.getPiece(oneForward) == null) {
                if (newRow == promotionRow) {
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, oneForward, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(myPosition, oneForward, null));
                }
                if (row == startRow) {
                    ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
                    if (board.getPiece(twoForward) == null) {
                        moves.add(new ChessMove(myPosition, twoForward, null));
                    }
                }
            }
            int[] captureCols = {col - 1, col + 1};
            for (int newCol : captureCols) {
                newRow = row + direction;
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    continue;
                }
                ChessPosition diagPos = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(diagPos);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (newRow == promotionRow) {
                        moves.add(new ChessMove(myPosition, diagPos, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, diagPos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, diagPos, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, diagPos, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, diagPos, null));
                    }
                }
            }
        }
        return moves;
    }
}
