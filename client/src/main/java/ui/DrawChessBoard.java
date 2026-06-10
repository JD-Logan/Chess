package ui;

import chess.*;

public class DrawChessBoard {

    private static void drawBottomWhite(ChessBoard board) {
        System.out.println(); // top border
        System.out.println("" + "  a " + "  b " + "  c " + " d " + "  e " + "  f " + "  g " + " h ");
        for (int row = 8; row >= 1; row--) {
            System.out.printf("%d", row);
            for (int col =1; col <=8; col++) {
                printSquare(board, row, col);
            }
            System.out.printf(" %d%n", row);
        }
        System.out.println("" + "  a " + "  b " + "  c " + " d " + "  e " + "  f " + "  g " + " h ");
        System.out.println();
    }

    private static void drawBottomBlack(ChessBoard board) {
        System.out.println(); // top border
        System.out.println("" + "  h " + "  g " + "  f " + " e " + "  d " + "  c " + "  b " + " a ");
        for (int row = 1; row <= 8; row++) {
            System.out.printf("%d", row);
            for (int col =8; col >=1; col--) {
                printSquare(board, row, col);
            }
            System.out.printf(" %d%n", row);
        }
        System.out.println("" + "  h " + "  g " + "  f " + " e " + "  d " + "  c " + "  b " + " a ");
        System.out.println();
    }

    public static void drawBoard(ChessBoard board, boolean whiteOnBottom) {
        if (whiteOnBottom) {
            drawBottomWhite(board);
        } else {
            drawBottomBlack(board);
        }
    }

    private static void printSquare(ChessBoard board, int row, int col) {
        boolean whiteSquare = (row + col) % 2 != 0;
        String bg = whiteSquare ? EscapeSequences.SET_BG_COLOR_GREEN
                : EscapeSequences.SET_BG_COLOR_DARK_GREEN;
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        String pieceStr = piece == null ? EscapeSequences.EMPTY : pieceString(piece);
        String pieceColor = piece == null ? ""
                : piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? EscapeSequences.SET_TEXT_COLOR_RED
                : EscapeSequences.SET_TEXT_COLOR_BLUE;

        System.out.print(bg + pieceColor + pieceStr + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static String pieceString(ChessPiece piece) {
        boolean white = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING -> white ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> white ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> white ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case  KNIGHT-> white ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case  ROOK-> white ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case  PAWN-> white ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }



}
