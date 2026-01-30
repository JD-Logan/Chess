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
