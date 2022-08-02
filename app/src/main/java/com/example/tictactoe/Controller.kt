package com.example.tictactoe

import Cell
import GameState
import Grid
import Player
import User
import android.util.Log
import com.example.tictactoe.gamescreen.GameScreenFragment
import com.example.tictactoe.settings.*

class Controller(
    val gameScreenFragment : GameScreenFragment,
    val settings: Settings
) : IController {

    override lateinit var gameState : GameState

    init {
        gameState = settings.gameState
    }

    override fun play(gameType: String) {
        when(gameType){
            "playerVsPlayer" -> playPlayerVsPlayer()
            "playerVsAi" -> playPlayerVsAi()
        }
    }

    override fun getPlayer(): Player {
        TODO("Not yet implemented")
    }

    override fun setPlayer(settingsFragment: SettingsFragment?) {
        TODO("Not yet implemented")
    }

    override fun getCellTypeImg(): Int {
        TODO("Not yet implemented")
    }

    override fun onGridCellSelected(row: Int, col: Int){
        // if the cell already been visited
        if(gameState.getCell(row,col).content != CellType.EMPTY) return

        val currentTurnCellType = gameState.currentTurn().cellType
        val imgId : Int = when(currentTurnCellType){
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
        gameScreenFragment.setCellImg(row, col, imgId)
    }

    private fun getCurrentPlayerAngMakeMove(gameState: GameState)  : Player{
        val currentPlayer = gameState.getNextPlayer()
        val (row, col) = currentPlayer.getNextMove(gameState)
        gameState.setCell(Cell(
            row = row,
            col = col,
            content = currentPlayer.cellType
        ))
        gameState.listOfMoves.add(Pair(row,col))
        return currentPlayer
    }

    private fun playPlayerVsPlayer(){
        TODO("Implementation")
    }

    private fun playPlayerVsAi(){
        gameState = GameState(
            grid = Grid(3),
            listOfPlayers = mutableListOf(
                User(
                    cellType = CellType.CROSS
                ) as Player,
                Agent.createAgent(
                    cellType = CellType.CIRCLE,
                    diff = AgentDifficulties.MEDIUM
                ) as Player
            ),
            notVisitedCell = Cell(),
            listOfMoves = mutableListOf()
        )

        play(gameState)
    }


    private fun createGameState(gameType:String){
        when(gameType){
            "playerVsAi" -> gameState = GameState(
                grid = Grid(3),
                listOfPlayers = mutableListOf(
                    User(
                        cellType = CellType.CROSS
                    ) as Player,
                    Agent.createAgent(
                        cellType = CellType.CIRCLE,
                        diff = AgentDifficulties.MEDIUM
                    ) as Player
                ),
                notVisitedCell = Cell(),
                listOfMoves = mutableListOf()
            )
        }
    }

    private fun play(gameState: GameState) {
        Display.toConsole("Welcome to TicTacToe !")
        var currentGameState = gameState.deepCopy()
        while (true) {
            Display.toConsole("$currentGameState")
            val currentPlayer = getCurrentPlayerAngMakeMove(currentGameState)
            val currentPlayerCellType = currentPlayer.cellType
            if (currentGameState.isWinState(Cell(content = currentPlayerCellType))
                || currentGameState.isStandOff()){
                when{
                    currentGameState.isWinState(Cell(content = currentPlayerCellType)) ->
                        Display.toConsole("Player $currentPlayerCellType WINS!!!!!")
                    currentGameState.isStandOff() ->
                        Display.toConsole("It's a standoff -_-")
                }
                Display.toConsole("$currentGameState")
                return
            }
            currentGameState = currentGameState.updateGameState()
        } // end game loop
    }

}