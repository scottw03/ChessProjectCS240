package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoardRenderer {
    public static void drawBoard(
            ChessBoard board,
            ChessGame.TeamColor perspective) {
        System.out.println();
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBlack(board);
    }
        else {
            drawWhite(board);
        }
    }

    public static void drawHighlightedBoard(
            ChessBoard board,
            ChessPosition selected,
            Collection<ChessMove> legalMoves,
            ChessGame.TeamColor perspective) {
        System.out.println();
        Set<ChessPosition> destinations =
                new HashSet<>();
        for (ChessMove move : legalMoves) {
            destinations.add(
                    move.getEndPosition());
        }
        if (perspective ==
        ChessGame.TeamColor.WHITE) {
            drawHighlightedWhitePerspective(
                    board,
                    selected,
                    destinations);
        }
        else {
            drawHighlightedBlackPerspective(
                    board,
                    selected,
                    destinations);
        }
    }

    private static void drawHighlightedSquare(
            ChessPiece piece,
            int row,
            int col,
            ChessPosition current,
            ChessPosition selected,
            Set<ChessPosition> destinations) {
        setSquareColor(
                row,
                col,
                current,
                selected,
                destinations);
        if (piece == null) {
            System.out.print(EscapeSequences.EMPTY);
        }
        else {
            if (piece.getTeamColor() ==
            ChessGame.TeamColor.WHITE) {
                System.out.print(
                        EscapeSequences.SET_TEXT_COLOR_BLUE);
            }
            else {
                System.out.print(
                        EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
            }
            System.out.print(
                    getPieceSymbol(piece));
        }
    }

    private static void drawHighlightedWhitePerspective(
            ChessBoard board,
            ChessPosition selected,
            Set<ChessPosition> destinations) {
        printColumnHeadersWhite();
        for (int row = 8; row >= 1; row--) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            for (int col = 1; col <= 8; col++) {
                ChessPosition current =
                        new ChessPosition(row, col);
                drawHighlightedSquare(
                        board.getPiece(current),
                        row,
                        col,
                        current,
                        selected,
                        destinations);
            }
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            System.out.println();
        }
        printColumnHeadersWhite();
    }

    private static void drawHighlightedBlackPerspective(
            ChessBoard board,
            ChessPosition selected,
            Set<ChessPosition> destinations) {
        printColumnHeadersBlack();
        for (int row = 1; row <= 8; row++) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            for (int col = 8; col >= 1; col--) {
                ChessPosition current =
                        new ChessPosition(row, col);
                drawHighlightedSquare(
                        board.getPiece(current),
                        row,
                        col,
                        current,
                        selected,
                        destinations);
            }
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            System.out.println();
        }
        printColumnHeadersBlack();
    }

    private static void resetColors() {
        System.out.print(
                EscapeSequences.RESET_BG_COLOR);
        System.out.print(
                EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printPiece(
            ChessPiece piece) {
        if (piece == null) {
            System.out.print("   ");
            return;
        }
        String symbol = getPieceSymbol(piece);
        System.out.print(symbol);
    }

    private static void setSquareColor(
            int row,
            int col,
            ChessPosition current,
            ChessPosition selected,
            Set<ChessPosition> destinations) {
        boolean lightSquare =
                (row + col) % 2 != 0;
        if (current.equals(selected)) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_YELLOW);
        }
        else if (destinations.contains(current)) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_GREEN);
        }
        else if (lightSquare) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        }
        else {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_DARK_GREY);
        }
    }

    private static void drawWhite(ChessBoard board) {
        printColumnHeadersWhite();
        for (int row = 8; row >= 1; row--) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            for (int col = 1; col <= 8; col ++) {
                drawSquare(
                        board.getPiece(
                                new ChessPosition(row, col)),
                        row,
                        col);
            }
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            System.out.println();
        }
        printColumnHeadersWhite();
    }

    private static void drawBlack(ChessBoard board) {
        printColumnHeadersBlack();
        for (int row = 1; row <= 8; row++) {
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            for (int col = 8; col >= 1; col--) {
                drawSquare(
                        board.getPiece(
                                new ChessPosition(row, col)),
                        row,
                        col);
            }
            System.out.print(
                    EscapeSequences.SET_BG_COLOR_BLACK +
                            EscapeSequences.SET_TEXT_COLOR_WHITE +
                            " " + row + " ");
            System.out.println();
        }
        printColumnHeadersBlack();
    }

    private static void printColumnHeadersWhite() {
        System.out.print(
                EscapeSequences.SET_BG_COLOR_BLACK +
                        EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.println(
                "    a   b   c  d   e  f   g   h ");
        System.out.print(
                EscapeSequences.RESET_BG_COLOR +
                        EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printColumnHeadersBlack() {
        System.out.print(
                EscapeSequences.SET_BG_COLOR_BLACK +
                        EscapeSequences.SET_TEXT_COLOR_WHITE);
        System.out.println(
                "    h   g   f  e   d  c   b   a ");
        System.out.print(
                EscapeSequences.RESET_BG_COLOR +
                        EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void drawSquare(
            ChessPiece piece,
            int row,
            int col) {
        boolean lightSquare =
             (row + col) % 2 != 0;

        String background =
             lightSquare
             ? EscapeSequences.SET_BG_COLOR_WHITE
                     : EscapeSequences.SET_BG_COLOR_DARK_GREEN;
        System.out.print(background);
        if (piece == null) {
            System.out.print(EscapeSequences.EMPTY);
        }
        else {
            if (piece.getTeamColor() ==
            ChessGame.TeamColor.WHITE) {
                System.out.print(
                        EscapeSequences.SET_TEXT_COLOR_BLUE);
            }
            else {
                System.out.print(
                        EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
            }
            System.out.print(getPieceSymbol(piece));
        }
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KING
                        : EscapeSequences.BLACK_KING;
            case QUEEN ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_QUEEN
                        : EscapeSequences.BLACK_QUEEN;
            case ROOK ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_ROOK
                        : EscapeSequences.BLACK_ROOK;
            case BISHOP ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_BISHOP
                        : EscapeSequences.BLACK_BISHOP;
            case KNIGHT ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KNIGHT
                        : EscapeSequences.BLACK_KNIGHT;
            case PAWN ->
                piece.getTeamColor() ==
                        ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_PAWN
                        : EscapeSequences.BLACK_PAWN;
        };
    }
}
