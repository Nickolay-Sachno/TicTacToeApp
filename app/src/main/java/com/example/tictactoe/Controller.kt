package com.example.tictactoe

import Cell
import GameState
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.tictactoe.gamescreen.GameScreenFragment
import com.example.tictactoe.settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Controller(
    private val gameScreenFragment : GameScreenFragment,
    val settings: Settings,
    val context: Context
) : IController {

    override var gameState : GameState = settings.gameState
    private var sharedPref : SharedPreferences = context.getSharedPreferences("database", MODE_PRIVATE)

    fun setDiffMode(diff :String){
        sharedPref.edit().putString("diff_key", diff).commit()
    }

    fun getDiffMode() : String? {
        return sharedPref.getString("diff_key", "easy")
    }

    override fun onGridCellSelected(row: Int, col: Int){
        // if the cell already been visited
        if(gameState.getCell(row,col).content != CellType.EMPTY) return

        when(settings.typeGame){
            "playerVsPlayer" -> playUser(row,col)
            "playerVsAi" -> {
                playUser(row,col)
                CoroutineScope(Main).launch {
                    delay(1000)
                    playAgent()
                }
            }
        }

    }

    override fun checkForWinner() {
        when{
            // first player wins
            gameState.isWinState(Cell(content = settings.firstPlayer.player.cellType)) -> {
                gameScreenFragment.navigateToEntryFragment("First player wins! ")
            }
            // second player wins
            gameState.isWinState(Cell(content = settings.secondPlayer.player.cellType)) -> {
                gameScreenFragment.navigateToEntryFragment("Second player wins! ")
            }
            // standoff
            gameState.isStandOff() -> {
                gameScreenFragment.navigateToEntryFragment("It's a standoff ")
            }
        }
    }

    override fun playAgent() {

        val currentPlayer = gameState.getNextPlayer()
        val imgId = CellTypeImg.CIRCLE_BLACK.id
        val (row, col) = currentPlayer.getNextMove(gameState)
        gameState.setCell(Cell(
            row = row,
            col = col,
            content = currentPlayer.cellType
        ))

        gameState.listOfMoves.add(Pair(row,col))
        gameState = gameState.updateGameState()

        // set turn img
        gameScreenFragment.setCellImg(-1,-1,CellTypeImg.CROSS_BLACK.id)
        // set grid cell img
        gameScreenFragment.setCellImg(row, col, imgId)

        checkForWinner()
    }

    override fun playUser(row: Int, col: Int) {

        val imgId : Int = when(gameState.currentTurn().cellType){
            CellType.CROSS -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CROSS
                ))
                gameScreenFragment.setCellImg(-1,-1,CellTypeImg.CIRCLE_BLACK.id)
                CellTypeImg.CROSS_BLACK.id
            }
            CellType.CIRCLE -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CIRCLE
                ))
                gameScreenFragment.setCellImg(-1,-1,CellTypeImg.CROSS_BLACK.id)
                CellTypeImg.CIRCLE_BLACK.id
            }
            else -> {0}
        }
        gameState.getNextPlayer()
        gameState.listOfMoves.add(Pair(row,col))
        gameState = gameState.updateGameState()
        gameScreenFragment.setCellImg(row, col, imgId)

        checkForWinner()
    }
}