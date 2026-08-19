package com.example.chessmobile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChessGameTest {
    @Test fun whitePawnCanOpenTwoSquares() {
        val game = ChessGame()
        assertTrue(game.move(Move(6, 4, 4, 4)))
        assertEquals(Side.BLACK, game.turn)
    }

    @Test fun rookCannotJumpPieces() {
        val game = ChessGame()
        assertTrue(game.legalMovesFrom(7, 0).isEmpty())
    }
}
