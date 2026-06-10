package ui;

import chess.*;

public class DrawChessBoard {

    private static void drawBoard(ChessBoard board) {
        System.out.println(); // top border
        System.out.println("    " + "  a  " + "  b  " + "  c  " + "  d  " + "  e  " + "  f  " + "  g  " + "  h  ");
        for (int row = 8; row >= 1; row--) {
            System.out.printf("%d", row);
            for (int col =1; col <=8; col++) {
                // print a square
            }
        }
    }
}
