package com.example.chessmobile

enum class Side { 
    WHITE, BLACK; 
    fun opposite() = if (this == WHITE) BLACK else WHITE 
}

enum class Kind(val white: String, val black: String) {
    KING("♔", "♚"), QUEEN("♕", "♛"), ROOK("♖", "♜"), BISHOP("♗", "♝"), KNIGHT("♘", "♞"), PAWN("♙", "♟")
}

data class Piece(val side: Side, val kind: Kind, val hasMoved: Boolean = false) {
    val symbol: String get() = if (side == Side.WHITE) kind.white else kind.black
}

data class Move(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int, val promotion: Kind? = null)

class ChessGame {
    var board: Array<Array<Piece?>> = Array(8) { arrayOfNulls<Piece>(8) }
    var turn: Side = Side.WHITE; private set
    var winner: Side? = null; private set
    var isStalemate: Boolean = false; private set

    val status: String get() = when {
        winner != null -> "Checkmate! ${winner!!.name.lowercase().replaceFirstChar(Char::uppercase)} wins!"
        isStalemate -> "Stalemate! Draw."
        isCheck(turn, board) -> "${turn.name.lowercase().replaceFirstChar(Char::uppercase)} is in check!"
        else -> "${turn.name.lowercase().replaceFirstChar(Char::uppercase)} to move"
    }

    init { reset() }

    fun reset() {
        board = Array(8) { arrayOfNulls<Piece>(8) }
        val order = listOf(Kind.ROOK, Kind.KNIGHT, Kind.BISHOP, Kind.QUEEN, Kind.KING, Kind.BISHOP, Kind.KNIGHT, Kind.ROOK)
        for (c in 0..7) {
            board[0][c] = Piece(Side.BLACK, order[c]); board[1][c] = Piece(Side.BLACK, Kind.PAWN)
            board[6][c] = Piece(Side.WHITE, Kind.PAWN); board[7][c] = Piece(Side.WHITE, order[c])
        }
        turn = Side.WHITE; winner = null; isStalemate = false
    }

    private fun getPseudoLegalMoves(row: Int, col: Int, b: Array<Array<Piece?>>): List<Move> {
        val piece = b[row][col] ?: return emptyList()
        val moves = mutableListOf<Move>()
        fun add(r: Int, c: Int): Boolean {
            if (r !in 0..7 || c !in 0..7) return false
            val target = b[r][c]
            if (target?.side == piece.side) return false
            moves += Move(row, col, r, c)
            return target == null
        }
        fun slide(dirs: List<Pair<Int, Int>>) = dirs.forEach { (dr, dc) ->
            var r = row + dr; var c = col + dc
            while (add(r, c)) { r += dr; c += dc }
        }
        when (piece.kind) {
            Kind.PAWN -> {
                val dir = if (piece.side == Side.WHITE) -1 else 1
                if (row + dir in 0..7 && b[row + dir][col] == null) {
                    moves += Move(row, col, row + dir, col)
                    val start = if (piece.side == Side.WHITE) 6 else 1
                    if (row == start && b[row + 2 * dir][col] == null) moves += Move(row, col, row + 2 * dir, col)
                }
                for (dc in listOf(-1, 1)) {
                    val r = row + dir; val c = col + dc
                    if (r in 0..7 && c in 0..7 && b[r][c]?.side == piece.side.opposite()) moves += Move(row, col, r, c)
                }
            }
            Kind.KNIGHT -> listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1).forEach { add(row + it.first, col + it.second) }
            Kind.BISHOP -> slide(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
            Kind.ROOK -> slide(listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
            Kind.QUEEN -> slide(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1))
            Kind.KING -> for (dr in -1..1) for (dc in -1..1) if (dr != 0 || dc != 0) add(row + dr, col + dc)
        }
        return moves
    }

    fun isCheck(side: Side, b: Array<Array<Piece?>>): Boolean {
        var kr = -1; var kc = -1
        for (r in 0..7) for (c in 0..7) if (b[r][c]?.kind == Kind.KING && b[r][c]?.side == side) { kr = r; kc = c }
        if (kr == -1) return false
        for (r in 0..7) for (c in 0..7) {
            val p = b[r][c]
            if (p != null && p.side != side) {
                if (getPseudoLegalMoves(r, c, b).any { it.toRow == kr && it.toCol == kc }) return true
            }
        }
        return false
    }

    fun legalMovesFrom(row: Int, col: Int): List<Move> {
        val piece = board[row][col] ?: return emptyList()
        if (piece.side != turn || winner != null) return emptyList()
        return getPseudoLegalMoves(row, col, board).filter { m ->
            val nextBoard = Array(8) { r -> board[r].copyOf() }
            nextBoard[m.toRow][m.toCol] = nextBoard[m.fromRow][m.fromCol]
            nextBoard[m.fromRow][m.fromCol] = null
            !isCheck(piece.side, nextBoard)
        }
    }

    fun move(move: Move): Boolean {
        val valid = legalMovesFrom(move.fromRow, move.fromCol)
        if (valid.none { it.fromRow == move.fromRow && it.fromCol == move.fromCol && it.toRow == move.toRow && it.toCol == move.toCol }) return false
        
        val piece = board[move.fromRow][move.fromCol] ?: return false
        board[move.toRow][move.toCol] = if (piece.kind == Kind.PAWN && (move.toRow == 0 || move.toRow == 7)) {
            Piece(piece.side, move.promotion ?: Kind.QUEEN, true)
        } else piece.copy(hasMoved = true)
        board[move.fromRow][move.fromCol] = null
        
        turn = turn.opposite()
        checkEndGame()
        return true
    }

    private fun checkEndGame() {
        val hasMoves = (0..7).any { r -> (0..7).any { c -> legalMovesFrom(r, c).isNotEmpty() } }
        if (!hasMoves) {
            if (isCheck(turn, board)) winner = turn.opposite()
            else isStalemate = true
        }
    }

    fun isPromotion(fromRow: Int, fromCol: Int, toRow: Int): Boolean {
        val piece = board[fromRow][fromCol] ?: return false
        return piece.kind == Kind.PAWN && (toRow == 0 || toRow == 7)
    }
}
