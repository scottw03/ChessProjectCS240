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
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        ChessPiece target = board.getPiece(endPosition);
        if (target == null) {
            return true;
        } else {
            return target.getTeamColor() != this.pieceColor;
        }
    }

    private void addDirectionalMoves(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition start,
            int rowDelta,
            int colDelta
    ) {
        int row = start.getRow();
        int col = start.getColumn();
        while (true) {
            row += rowDelta;
            col += colDelta;
            ChessPosition newPos =
                    new ChessPosition(row, col);
            if (!viableDestination(board, newPos)) {
                break;
            }
            moves.add(
                    new ChessMove(start, newPos, null)
            );
            if (board.getPiece(newPos) != null) {
                break;
            }
        }
    }

    private Collection<ChessMove> rookMoves(
            ChessBoard board,
            ChessPosition position
    ) {
        Collection<ChessMove> moves =
                new ArrayList<>();
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };
        for (int[] dir : directions) {
            addDirectionalMoves(
                    moves,
                    board,
                    position,
                    dir[0],
                    dir[1]
            );
        }
        return moves;
    }

    private Collection<ChessMove> bishopMoves(
            ChessBoard board,
            ChessPosition position
    ) {
        Collection<ChessMove> moves =
                new ArrayList<>();
        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
        for (int[] dir : directions) {
            addDirectionalMoves(
                    moves,
                    board,
                    position,
                    dir[0],
                    dir[1]
            );
        }
        return moves;
    }

    private Collection<ChessMove> queenMoves(
            ChessBoard board,
            ChessPosition position
    ) {
        Collection<ChessMove> moves =
                new ArrayList<>();
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
        for (int[] dir : directions) {
            addDirectionalMoves(
                    moves,
                    board,
                    position,
                    dir[0],
                    dir[1]
            );
        }
        return moves;
    }

    private void addSingleMoves(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition start,
            int[][] directions
    ) {
        for (int[] dir : directions) {
            int row = start.getRow() + dir[0];
            int col = start.getColumn() + dir[1];
            ChessPosition newPos =
                    new ChessPosition(row, col);
            if (viableDestination(board, newPos)) {
                moves.add(
                        new ChessMove(
                                start,
                                newPos,
                                null
                        )
                );
            }
        }
    }

    private Collection<ChessMove> knightMoves(
            ChessBoard board,
            ChessPosition position
    ) {

        Collection<ChessMove> moves =
                new ArrayList<>();

        int[][] directions = {
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2},
                {2, 1},
                {2, -1},
                {-2, 1},
                {-2, -1}
        };

        addSingleMoves(
                moves,
                board,
                position,
                directions
        );

        return moves;
    }

    private Collection<ChessMove> kingMoves(
            ChessBoard board,
            ChessPosition position
    ) {

        Collection<ChessMove> moves =
                new ArrayList<>();

        int[][] directions = {
                {1, 1},
                {1, 0},
                {1, -1},
                {0, 1},
                {0, -1},
                {-1, 1},
                {-1, 0},
                {-1, -1}
        };

        addSingleMoves(
                moves,
                board,
                position,
                directions
        );

        addCastleMoves(
                moves,
                board,
                position
        );

        return moves;
    }

    private void addPromotionMoves(
            Collection<ChessMove> moves,
            ChessPosition start,
            ChessPosition end
    ) {

        moves.add(
                new ChessMove(start, end, PieceType.QUEEN)
        );
        moves.add(
                new ChessMove(start, end, PieceType.ROOK)
        );
        moves.add(
                new ChessMove(start, end, PieceType.BISHOP)
        );
        moves.add(
                new ChessMove(start, end, PieceType.KNIGHT)
        );
    }

    private Collection<ChessMove> pawnMoves(
            ChessBoard board,
            ChessPosition position
    ) {

        Collection<ChessMove> moves =
                new ArrayList<>();

        int direction =
                (pieceColor == ChessGame.TeamColor.WHITE)
                        ? 1
                        : -1;

        int startRow =
                (pieceColor == ChessGame.TeamColor.WHITE)
                        ? 2
                        : 7;

        int promotionRow =
                (pieceColor == ChessGame.TeamColor.WHITE)
                        ? 8
                        : 1;

        addForwardPawnMoves(
                moves,
                board,
                position,
                direction,
                startRow,
                promotionRow
        );

        addCapturePawnMoves(
                moves,
                board,
                position,
                direction,
                promotionRow
        );

        return moves;
    }

    private void addForwardPawnMoves(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition start,
            int direction,
            int startRow,
            int promotionRow
    ) {

        int row = start.getRow();
        int col = start.getColumn();

        int newRow = row + direction;

        if (!isValidPosition(newRow, col)) {
            return;
        }

        ChessPosition oneForward =
                new ChessPosition(newRow, col);

        if (board.getPiece(oneForward) != null) {
            return;
        }

        addPawnMove(
                moves,
                start,
                oneForward,
                promotionRow
        );

        if (row != startRow) {
            return;
        }

        ChessPosition twoForward =
                new ChessPosition(
                        row + (2 * direction),
                        col
                );

        if (board.getPiece(twoForward) == null) {

            moves.add(
                    new ChessMove(
                            start,
                            twoForward,
                            null
                    )
            );
        }
    }

    private void addCapturePawnMoves(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition start,
            int direction,
            int promotionRow
    ) {

        int row = start.getRow();
        int col = start.getColumn();

        int[] captureColumns = {
                col - 1,
                col + 1
        };

        for (int captureCol : captureColumns) {

            int captureRow = row + direction;

            if (!isValidPosition(
                    captureRow,
                    captureCol
            )) {

                continue;
            }

            ChessPosition diagonal =
                    new ChessPosition(
                            captureRow,
                            captureCol
                    );

            ChessPiece target =
                    board.getPiece(diagonal);

            if (target == null) {
                continue;
            }

            if (target.getTeamColor() == pieceColor) {
                continue;
            }

            addPawnMove(
                    moves,
                    start,
                    diagonal,
                    promotionRow
            );
        }
    }

    private void addPawnMove(
            Collection<ChessMove> moves,
            ChessPosition start,
            ChessPosition end,
            int promotionRow
    ) {

        if (end.getRow() == promotionRow) {

            addPromotionMoves(
                    moves,
                    start,
                    end
            );

            return;
        }

        moves.add(
                new ChessMove(
                        start,
                        end,
                        null
                )
        );
    }

    private void addCastleMoves(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition kingPosition
    ) {

        if (hasMoved) {
            return;
        }

        addKingSideCastle(
                moves,
                board,
                kingPosition
        );

        addQueenSideCastle(
                moves,
                board,
                kingPosition
        );
    }

    private void addKingSideCastle(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition kingPosition
    ) {

        int row = kingPosition.getRow();

        ChessPosition rookPosition =
                new ChessPosition(row, 8);

        ChessPiece rook =
                board.getPiece(rookPosition);

        if (!validCastleRook(rook)) {
            return;
        }

        ChessPosition f =
                new ChessPosition(row, 6);

        ChessPosition g =
                new ChessPosition(row, 7);

        if (board.getPiece(f) != null
                || board.getPiece(g) != null) {

            return;
        }

        moves.add(
                new ChessMove(
                        kingPosition,
                        g,
                        null
                )
        );
    }

    private void addQueenSideCastle(
            Collection<ChessMove> moves,
            ChessBoard board,
            ChessPosition kingPosition
    ) {

        int row = kingPosition.getRow();

        ChessPosition rookPosition =
                new ChessPosition(row, 1);

        ChessPiece rook =
                board.getPiece(rookPosition);

        if (!validCastleRook(rook)) {
            return;
        }

        ChessPosition b =
                new ChessPosition(row, 2);

        ChessPosition c =
                new ChessPosition(row, 3);

        ChessPosition d =
                new ChessPosition(row, 4);

        if (board.getPiece(b) != null
                || board.getPiece(c) != null
                || board.getPiece(d) != null) {

            return;
        }

        moves.add(
                new ChessMove(
                        kingPosition,
                        c,
                        null
                )
        );
    }

    private boolean validCastleRook(
            ChessPiece rook
    ) {

        return rook != null
                && rook.getPieceType() == PieceType.ROOK
                && !rook.hasMoved();
    }

    private boolean isValidPosition(
            int row,
            int col
    ) {

        return row >= 1
                && row <= 8
                && col >= 1
                && col <= 8;
    }


    public Collection<ChessMove> pieceMoves(
            ChessBoard board,
            ChessPosition myPosition
    ) {
        return switch (type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }
}
