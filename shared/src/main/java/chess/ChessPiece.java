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
//        ChessPiece piece = board.getPiece(myPosition);
//        if (piece.getPieceType() == PieceType.BISHOP) {
//            return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null)); // hardcoded
//        }
//        return List.of();
        // Bishop moves idea
        if (this.type == PieceType.BISHOP) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 1); // UP-RIGHT
            longRangeMoves(board, myPosition, moves, -1, 1); // UP-LEFT
            longRangeMoves(board, myPosition, moves, 1, -1); // DOWN-RIGHT
            longRangeMoves(board, myPosition, moves, -1, -1); // DOWN-LEFT
            return moves;

            // ROOK
        } else if (this.type == PieceType.ROOK) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 0); // UP
            longRangeMoves(board, myPosition, moves, -1, 0); // DOWN
            longRangeMoves(board, myPosition, moves, 0, -1); // LEFT
            longRangeMoves(board, myPosition, moves, 0, 1); // RIGHT

            return moves;

            // QUEEN
        } else if (this.type == PieceType.QUEEN) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 0); // UP
            longRangeMoves(board, myPosition, moves, -1, 0); // DOWN
            longRangeMoves(board, myPosition, moves, 0, -1); // LEFT
            longRangeMoves(board, myPosition, moves, 0, 1); // RIGHT
            longRangeMoves(board, myPosition, moves, 1, 1); // UP-RIGHT
            longRangeMoves(board, myPosition, moves, -1, 1); // UP-LEFT
            longRangeMoves(board, myPosition, moves, 1, -1); // DOWN-RIGHT
            longRangeMoves(board, myPosition, moves, -1, -1); // DOWN-LEFT

            return moves;

        } else if (this.type == PieceType.KING) {
            List<ChessMove> moves = new ArrayList<>();

            singleRangeMove(board, myPosition, moves,1,-1); // UP-LEFT
            singleRangeMove(board, myPosition, moves,1,0); // UP
            singleRangeMove(board, myPosition, moves,1,1); // UP-RIGHT
            singleRangeMove(board, myPosition, moves,0,-1); // LEFT
            singleRangeMove(board, myPosition, moves,0,1); // RIGHT
            singleRangeMove(board, myPosition, moves,-1,-1); // DOWN-LEFT
            singleRangeMove(board, myPosition, moves,-1,0); // DOWN
            singleRangeMove(board, myPosition, moves,-1,1); // DOWN-RIGHT

            return moves;
        } else if (this.type == PieceType.KNIGHT) {
            List<ChessMove> moves = new ArrayList<>();

            singleRangeMove(board, myPosition, moves,1,-2); // LEFT-UP
            singleRangeMove(board, myPosition, moves,2,-1); // UP-LEFT
            singleRangeMove(board, myPosition, moves,2,1); // UP-RIGHT
            singleRangeMove(board, myPosition, moves,1,2); // RIGHT-UP
            singleRangeMove(board, myPosition, moves,-1,2); // RIGHT_DOWN
            singleRangeMove(board, myPosition, moves,-2,1); // DOWN-RIGHT
            singleRangeMove(board, myPosition, moves,-2,-1); // DOWN-LEFT
            singleRangeMove(board, myPosition, moves,-1,-2); // LEFT-DOWN

            return moves;


            // PAWN
            // NOT COMPLETE
            // NOTE: pawns can only move one direction which is gonna make this weird because white and black need to go their ways. so set a flag for white or black to determine the direction.
            // also probably just do knight and king and do the pawn last.
        } else if (this.type == PieceType.PAWN) {
            List<ChessMove> moves = new ArrayList<>();
//
//            singleRangeMove(board, myPosition, moves,1,0); // UP
//
//            int row = myPosition.getRow();
//            int col = myPosition.getColumn();
//            int rowUp = row + 1;
//            int colLeft = col - 1;
//            int colRight = col + 1;
//
//            if (row>=1 && row <=8 && col >=1 && col <=8) {
//                ChessPosition leftPos = new ChessPosition(rowUp, colLeft);
//                ChessPiece pieceAtPos = board.getPiece(leftPos);
//
//                if (pieceAtPos != null && pieceAtPos.getTeamColor() != this.pieceColor) {
//                    moves.add(new ChessMove(myPosition, leftPos, null));
//                }
//                ChessPosition newPos = new ChessPosition(rowUp, colRight);
//                ChessPiece pieceAtPos2 = board.getPiece(newPos);
//
//                if (pieceAtPos2 != null && pieceAtPos2.getTeamColor() != this.pieceColor) {
//                    moves.add(new ChessMove(myPosition, newPos, null));
//                }
//            }

            // this is all kinda chopped because they have black or white
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            // the direction part of this

            if (row>=1 && row <=8 && col >=1 && col <=8) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = board.getPiece(newPos);

                if (pieceAtPos == null || pieceAtPos.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }

            return moves;

        } else {
            return List.of();
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

    private void longRangeMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int rowChange, int colChange) {
        int row = myPosition.getRow() + rowChange;
        int col = myPosition.getColumn() + colChange;

        while (row >=1 && row <=8 && col >=1 && col <=8) {
            ChessPosition newPos = new ChessPosition(row,col);
            ChessPiece pieceAtpos = board.getPiece(newPos);

            if (pieceAtpos == null) {
                moves.add(new ChessMove(myPosition, newPos, null)); // empty spot
            } else if(pieceAtpos.getTeamColor() != this.pieceColor) {
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
