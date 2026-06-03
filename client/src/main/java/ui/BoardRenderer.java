package ui;

import chess.*;

public class BoardRenderer {
    public static void drawBoard(
            ChessBoard board,
            ChessGame.TeamColor perspective) {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBlack(board);
    }
        else {
            drawWhite(board);
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
                "     h   g   f   e   d  c   b   a ");
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
