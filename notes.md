"# My notes" 

p1

start with isInCheck because all the others depend on it.

order of functions to do:
- isInCheck(TeamColor teamColor)
  - written dependencies
- validMoves(ChessPosition startPosition)
  - is needed by makeMove and both Checkmate and Stalemate
- makeMove(ChessMove move)
  - uses validMoves
- isInCheckmate(TeamColor teamColor)
  - uses isInCheck and validMoves
- isInStalemate(TeamColor teamColor)
  - uses isInCheck and validMoves

isInCheck:
- plan:
  - scan the board for the king
  - get all the piece moves for the opponents pieces
  - if any of the moves captures the kings position, return true

validMoves:
- plan:
  - get piece at startPosition
  - call piece.pieceMoves and use isInCheck to filter out invalid moves
    - maybe copy the board for each move? if king is not in check after move, keep the move

makeMove:
- check if the piece color is the correct teams turn
- get validMoves for start position
- don't let them make an invalid move
- make move
  - remove piece from startPosition
  - place piece at endPosition
  - if needed, promote
- change team turn

isInCheckmate:
- if not in check to begin with, return false
- get all same teams pieces
- run validMoves on pieces
- if any moves exist, return false
- if in check and no moves, return true

isInStalemate:

- if in check already, return false
- get all same teams pieces
- run validMoves on pieces
- if any moves exist, return false
- if not in check and no moves, return true



potential helper methods:
scanForKing
simulateBoardMove
- leavesKingInCheck (maybe different maybe not)
getTeamsPieces



What's working:
isInCheck / Check
validMoves / creating valid moves list

Extra Credit Moves
If you would like to fully implement the rules of chess you need to provide support for Castling and En Passant.
You do not have to implement these moves, but if you go the extra mile and successfully implement them, you’ll earn 5 extra credit points for each move (10 total) on this assignment.

 Castling
This is a special move where the King and a Rook move simultaneously. The castling move can only be taken when 3 conditions are met:

Neither the King nor Rook have moved since the game started
There are no pieces between the King and the Rook
The King is never in Check. The King does not start in Check, does not cross a square on which it would be in Check, and is not in Check after castling.
To Castle, the King moves 2 spaces towards the Rook, and the Rook "jumps" the king moving to the position next to and on the other side of the King.
 This is represented in a ChessMove as the king moving 2 spaces to the side.

 En Passant
This is a special move taken by a Pawn in response to your opponent double moving a Pawn.
 If your opponent double moves a pawn so it ends next to yours (skipping the position where your pawn could have captured their pawn),
 then on your immediately following turn your pawn may capture their pawn as if their pawn had only moved 1 square.
 This is as if your pawn is capturing their pawn mid motion, or "In Passing."