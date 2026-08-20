package com.example.chessmobile

import org.junit.Test
import org.junit.Assert.*

class ChessGameTest {
    @Test
    fun whitePawnCanOpenTwoSquares() {
        val game = ChessGame()
        assertTrue(game.move(Move(6, 4, 4, 4)))
        assertEquals(Side.BLACK, game.turn)
    }

    @Test
    fun cannotMoveIntoCheck() {
        val game = ChessGame()
        // Clear pieces around King to setup a check scenario
        game.board[6][4] = null // e2
        game.board[1][4] = null // e7
        // Black Queen pins White King (hypothetically if we moved pieces)
        game.board[0][3] = null // Clear original queen
        game.board[4][4] = Piece(Side.BLACK, Kind.QUEEN)
        // Try to move King into check
        assertFalse(game.move(Move(7, 4, 6, 4)))
    }

    @Test
    fun checkmateDetection() {
        val game = ChessGame()
        // Fool's Mate setup
        game.move(Move(6, 5, 5, 5)) // f3
        game.move(Move(1, 4, 3, 4)) // e5
        game.move(Move(6, 6, 4, 6)) // g4
        game.move(Move(0, 3, 4, 7)) // Qh4#
        assertEquals(Side.BLACK, game.winner)
    }
}
