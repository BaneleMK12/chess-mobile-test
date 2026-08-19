package com.example.chessmobile

enum class Side { WHITE, BLACK }
enum class Kind(val white: String, val black: String) {
    KING("♔", "♚"), QUEEN("♕", "♛"), ROOK("♖", "♜"), BISHOP("♗", "♝"), KNIGHT("♘", "♞"), PAWN("♙", "♟")
}

data class Piece(val side: Side, val kind: Kind) { val symbol: String get() = if (side == Side.WHITE) kind.white else kind.black }
data class Move(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int)

class ChessGame {
    val board: Array<Array<Piece?>> = Array(8) { arrayOfNulls<Piece>(8) }
    var turn: Side = Side.WHITE; private set
    var winner: Side? = null; private set
    val status: String get() = winner?.let { "${it.name.lowercase().replaceFirstChar(Char::uppercase)} wins!" }
        ?: "${turn.name.lowercase().replaceFirstChar(Char::uppercase)} to move"

    init { reset() }

    fun reset() {
        for (r in 0..7) for (c in 0..7) board[r][c] = null
        val order = listOf(Kind.ROOK, Kind.KNIGHT, Kind.BISHOP, Kind.QUEEN, Kind.KING, Kind.BISHOP, Kind.KNIGHT, Kind.ROOK)
        for (c in 0..7) {
            board[0][c] = Piece(Side.BLACK, order[c]); board[1][c] = Piece(Side.BLACK, Kind.PAWN)
            board[6][c] = Piece(Side.WHITE, Kind.PAWN); board[7][c] = Piece(Side.WHITE, order[c])
        }
        turn = Side.WHITE; winner = null
    }

    fun legalMovesFrom(row: Int, col: Int): List<Move> {
        val piece = board.getOrNull(row)?.getOrNull(col) ?: return emptyList()
        if (piece.side != turn || winner != null) return emptyList()
        val moves = mutableListOf<Move>()
        fun add(r: Int, c: Int): Boolean {
            if (r !in 0..7 || c !in 0..7) return false
            val target = board[r][c]
            if (target?.side == piece.side) return false
            moves += Move(row, col, r, c)
            return target == null
        }
        fun slide(vararg dirs: Pair<Int, Int>) = dirs.forEach { (dr, dc) ->
            var r = row + dr; var c = col + dc
            while (add(r, c)) { r += dr; c += dc }
        }
        when (piece.kind) {
            Kind.PAWN -> {
                val dir = if (piece.side == Side.WHITE) -1 else 1
                val start = if (piece.side == Side.WHITE) 6 else 1
                if (row + dir in 0..7 && board[row + dir][col] == null) {
                    moves += Move(row, col, row + dir, col)
                    if (row == start && board[row + 2 * dir][col] == null) moves += Move(row, col, row + 2 * dir, col)
                }
                for (dc in listOf(-1, 1)) {
                    val r = row + dir; val c = col + dc
                    if (r in 0..7 && c in 0..7 && board[r][c]?.side == piece.side.opposite()) moves += Move(row, col, r, c)
                }
            }
            Kind.KNIGHT -> listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1).forEach { add(row + it.first, col + it.second) }
            Kind.BISHOP -> slide(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
            Kind.ROOK -> slide(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
            Kind.QUEEN -> slide(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
            Kind.KING -> for (dr in -1..1) for (dc in -1..1) if (dr != 0 || dc != 0) add(row + dr, col + dc)
        }
        return moves
    }

    fun move(move: Move): Boolean {
        if (move !in legalMovesFrom(move.fromRow, move.fromCol)) return false
        val captured = board[move.toRow][move.toCol]
        val piece = board[move.fromRow][move.fromCol] ?: return false
        winner = if (captured?.kind == Kind.KING) piece.side else null
        board[move.toRow][move.toCol] = if (piece.kind == Kind.PAWN && move.toRow in listOf(0, 7)) piece.copy(kind = Kind.QUEEN) else piece
        board[move.fromRow][move.fromCol] = null
        if (winner == null) turn = turn.opposite()
        return true
    }
}

private fun Side.opposite() = if (this == Side.WHITE) Side.BLACK else Side.WHITE
