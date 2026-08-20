package com.example.chessmobile

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val game = ChessGame()
    private val cells = Array(8) { arrayOfNulls<TextView>(8) }
    private lateinit var status: TextView
    private lateinit var boardLayout: GridLayout
    private var selected: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 32, 32, 32)
            backgroundColor = Color.rgb(240, 240, 240)
        }
        
        status = TextView(this).apply {
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(38, 50, 56))
            setPadding(0, 0, 0, 48)
        }
        
        val boardContainer = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        boardLayout = GridLayout(this).apply {
            rowCount = 8
            columnCount = 8
            useDefaultMargins = false
            backgroundColor = Color.rgb(33, 33, 33)
        }
        
        for (r in 0..7) for (c in 0..7) {
            val cell = TextView(this).apply {
                gravity = Gravity.CENTER
                textSize = 32f
                setOnClickListener { onCellTapped(r, c) }
            }
            boardLayout.addView(cell, GridLayout.LayoutParams(GridLayout.spec(r, 1f), GridLayout.spec(c, 1f)).apply {
                width = 0; height = 0
            })
            cells[r][c] = cell
        }

        // Ensure the board is a perfect square on mobile
        boardLayout.post {
            val side = boardLayout.width
            boardLayout.layoutParams.height = side
            boardLayout.requestLayout()
        }
        
        val reset = Button(this).apply {
            text = "New Game"
            setOnClickListener { 
                game.reset()
                selected = null
                render() 
            }
        }
        
        boardContainer.addView(boardLayout)
        root.addView(status)
        root.addView(boardContainer)
        root.addView(reset)
        setContentView(root)
        render()
    }

    private fun onCellTapped(row: Int, col: Int) {
        val current = selected
        if (current == null) {
            if (game.legalMovesFrom(row, col).isNotEmpty()) {
                selected = row to col
                render()
            }
        } else {
            if (game.isPromotion(current.first, current.second, row)) {
                showPromotionDialog(current.first, current.second, row, col)
            } else {
                val moved = game.move(Move(current.first, current.second, row, col))
                if (!moved && game.legalMovesFrom(row, col).isNotEmpty()) {
                    selected = row to col
                } else {
                    selected = null
                    if (!moved) Toast.makeText(this, "Illegal move", Toast.LENGTH_SHORT).show()
                }
                render()
            }
        }
    }

    private fun showPromotionDialog(fromR: Int, fromC: Int, toR: Int, toC: Int) {
        val pieces = arrayOf("Queen", "Knight", "Rook", "Bishop")
        val kinds = arrayOf(Kind.QUEEN, Kind.KNIGHT, Kind.ROOK, Kind.BISHOP)
        AlertDialog.Builder(this)
            .setTitle("Promote to")
            .setItems(pieces) { _, which ->
                game.move(Move(fromR, fromC, toR, toC, kinds[which]))
                selected = null
                render()
            }
            .setCancelable(false)
            .show()
    }

    private fun render() {
        // Native animation for move transitions
        TransitionManager.beginDelayedTransition(boardLayout, AutoTransition().apply { duration = 200 })
        
        status.text = game.status
        val legalTargets = selected?.let { game.legalMovesFrom(it.first, it.second).map { m -> m.toRow to m.toCol }.toSet() }.orEmpty()
        
        for (r in 0..7) for (c in 0..7) {
            cells[r][c]?.apply {
                text = game.board[r][c]?.symbol.orEmpty()
                setBackgroundColor(when {
                    selected == r to c -> Color.rgb(255, 238, 88)
                    r to c in legalTargets -> Color.rgb(197, 225, 165)
                    (r + c) % 2 == 0 -> Color.rgb(238, 238, 210)
                    else -> Color.rgb(118, 150, 86)
                })
            }
        }
    }
}
