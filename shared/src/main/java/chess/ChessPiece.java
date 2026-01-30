package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();

        switch (this.type) {
            case ROOK -> {
                slidingRangeMove(board, myPosition, moves, 1, 0);
                slidingRangeMove(board, myPosition, moves, 0, 1);
                slidingRangeMove(board, myPosition, moves, -1, 0);
                slidingRangeMove(board, myPosition, moves, 0, -1);
            }
            case BISHOP -> {
                slidingRangeMove(board, myPosition, moves, 1, 1);
                slidingRangeMove(board, myPosition, moves, -1, 1);
                slidingRangeMove(board, myPosition, moves, -1, -1);
                slidingRangeMove(board, myPosition, moves, 1, -1);
            }
            case QUEEN -> {
                slidingRangeMove(board, myPosition, moves, 1, 0);
                slidingRangeMove(board, myPosition, moves, 0, 1);
                slidingRangeMove(board, myPosition, moves, -1, 0);
                slidingRangeMove(board, myPosition, moves, 0, -1);
                slidingRangeMove(board, myPosition, moves, 1, 1);
                slidingRangeMove(board, myPosition, moves, -1, 1);
                slidingRangeMove(board, myPosition, moves, -1, -1);
                slidingRangeMove(board, myPosition, moves, 1, -1);
            }
            case KING -> {
                singleRangeMove(board, myPosition, moves, 1, 0);
                singleRangeMove(board, myPosition, moves, 0, 1);
                singleRangeMove(board, myPosition, moves, -1, 0);
                singleRangeMove(board, myPosition, moves, 0, -1);
                singleRangeMove(board, myPosition, moves, 1, 1);
                singleRangeMove(board, myPosition, moves, -1, 1);
                singleRangeMove(board, myPosition, moves, -1, -1);
                singleRangeMove(board, myPosition, moves, 1, -1);
            }
            case KNIGHT -> {
                singleRangeMove(board, myPosition, moves, 1, 2);
                singleRangeMove(board, myPosition, moves, 2, 1);
                singleRangeMove(board, myPosition, moves, 2, -1);
                singleRangeMove(board, myPosition, moves, 1, -2);
                singleRangeMove(board, myPosition, moves, -1, -2);
                singleRangeMove(board, myPosition, moves, -2, -1);
                singleRangeMove(board, myPosition, moves, -2, 1);
                singleRangeMove(board, myPosition, moves, -1, 2);
            }
            case PAWN -> {
                pawnMoves(board, myPosition, moves);
            }
            case null, default -> {
            }
        }

        return moves;
    }

    private void pawnMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int teamDirection = (ChessGame.TeamColor.WHITE == this.pieceColor) ? 1 : -1;
        int pawnFirstMove = (ChessGame.TeamColor.WHITE == this.pieceColor) ? 2 : 7;

        int rowAhead = myPosition.getRow() + teamDirection;
        int col = myPosition.getColumn();

        if (rowAhead >= 1 && rowAhead <= 8 && col >= 1 && col <= 8) {
            ChessPosition posAhead = new ChessPosition(rowAhead, col);
            ChessPiece pieceAtPosAhead = board.getPiece(posAhead);

            if (pieceAtPosAhead == null) {
                promotePawn(myPosition, moves, rowAhead, posAhead);
                if (myPosition.getRow() == pawnFirstMove) {
                    ChessPosition posTwoAhead = new ChessPosition(rowAhead + teamDirection, col);
                    ChessPiece pieceAtTwoAhead = board.getPiece(posTwoAhead);

                    if (pieceAtTwoAhead == null) {
                        moves.add(new ChessMove(myPosition, posTwoAhead, null));
                    }
                }
            }
        }

        List<Integer> diagonalCapture = List.of(-1, 1);
        for (Integer integer : diagonalCapture) {
            int capCol = col + integer;

            if (rowAhead >= 1 && rowAhead <= 8 && capCol >= 1 && capCol <= 8) {
                ChessPosition newPos = new ChessPosition(rowAhead, capCol);
                ChessPiece pieceAtPos = board.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() != this.pieceColor) {
                    promotePawn(myPosition, moves, rowAhead, newPos);
                }
            }
        }
    }

    private void promotePawn(ChessPosition myPosition, List<ChessMove> moves, int rowAhead, ChessPosition newPos) {
        if (rowAhead == 8 || rowAhead == 1) {
            moves.add(new ChessMove(myPosition, newPos, PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, newPos, PieceType.KNIGHT));
            moves.add(new ChessMove(myPosition, newPos, PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, newPos, PieceType.ROOK));
        } else {
            moves.add(new ChessMove(myPosition, newPos, null));
        }
    }

    private void singleRangeMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int rowChange, int colChange) {
        int row = myPosition.getRow() + rowChange;
        int col = myPosition.getColumn() + colChange;

        if (row>=1 && row <=8 && col >=1 && col <=8) {
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece pieceAtPos = board.getPiece(newPos);

            if (pieceAtPos == null || pieceAtPos.getTeamColor() != this.pieceColor) {
                moves.add(new ChessMove(myPosition, newPos, null));
            }
        }
    }

    private void slidingRangeMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int rowChange, int colChange) {
        int row = myPosition.getRow() + rowChange;
        int col = myPosition.getColumn() + colChange;

        while (row >=1 && row <=8 && col >=1 && col <=8) {
            ChessPosition newPos = new ChessPosition(row,col);
            ChessPiece pieceAtPos = board.getPiece(newPos);

            if (pieceAtPos == null) {
                moves.add(new ChessMove(myPosition, newPos, null)); // empty spot
            } else if(pieceAtPos.getTeamColor() != this.pieceColor) {
                moves.add(new ChessMove(myPosition, newPos, null)); // enemy piece
                break;
            } else {
                break; // Same team piece
            }
            row += rowChange;
            col += colChange;
        }
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
}
