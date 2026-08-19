package com.example.chessmobile

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
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
    private var selected: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Kotlin Chess"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 18, 18, 18) }
        status = TextView(this).apply { textSize = 22f; gravity = Gravity.CENTER; setTextColor(Color.rgb(38, 50, 56)) }
        val board = GridLayout(this).apply { rowCount = 8; columnCount = 8; useDefaultMargins = false }
        for (r in 0..7) for (c in 0..7) {
            val cell = TextView(this).apply {
                gravity = Gravity.CENTER; textSize = 34f; setTextColor(Color.rgb(33, 33, 33))
                setOnClickListener { onCellTapped(r, c) }
            }
            board.addView(cell, GridLayout.LayoutParams(GridLayout.spec(r, 1f), GridLayout.spec(c, 1f)).apply {
                width = 0; height = 0; setMargins(1, 1, 1, 1)
            })
            cells[r][c] = cell
        }
        val reset = Button(this).apply { text = "New game"; setOnClickListener { game.reset(); selected = null; render() } }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        root.addView(board, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(reset, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        render()
    }

    private fun onCellTapped(row: Int, col: Int) {
        val current = selected
        if (current == null) {
            if (game.legalMovesFrom(row, col).isNotEmpty()) selected = row to col
        } else {
            val moved = game.move(Move(current.first, current.second, row, col))
            selected = null
            if (!moved && game.legalMovesFrom(row, col).isNotEmpty()) selected = row to col
            if (!moved) Toast.makeText(this, "Illegal move", Toast.LENGTH_SHORT).show()
        }
        render()
    }

    private fun render() {
        status.text = game.status
        val legalTargets = selected?.let { game.legalMovesFrom(it.first, it.second).map { m -> m.toRow to m.toCol }.toSet() }.orEmpty()
        for (r in 0..7) for (c in 0..7) {
            cells[r][c]?.apply {
                text = game.board[r][c]?.symbol.orEmpty()
                setBackgroundColor(when {
                    selected == r to c -> Color.rgb(255, 213, 79)
                    r to c in legalTargets -> Color.rgb(174, 213, 129)
                    (r + c) % 2 == 0 -> Color.rgb(238, 238, 210)
                    else -> Color.rgb(118, 150, 86)
                })
            }
        }
    }
}
