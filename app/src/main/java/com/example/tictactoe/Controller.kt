package com.example.tictactoe

import Cell
import GameState
import Grid
import Player
import User
import com.example.tictactoe.entry.IEntryView
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.gamescreen.IGameScreenView
import com.example.tictactoe.settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


private const val FIRST_PLAYER_WIN = "First Player Wins!"
private const val SECOND_PLAYER_WINS = "Second Player Wins!"
private const val STANDOFF  = "It's a Standoff"
private const val LOCK = "LOCK"
private const val UNLOCK = "UNLOCK"

object Controller : IController {

    var settings: Settings = Settings()
    var fragment: View? = null

    override fun onGridCellSelected(row: Int, col: Int){
        // lock user from typing on screen
        fragment?.let { lockUserScreen(it) }

        var gameState : GameState = settings.gameState
        // if the cell already been visited
        if(gameState.getCell(row,col).content != CellType.EMPTY) return

        when(settings.typeGame){
            GameType.PLAYER_VS_PLAYER -> playUser(row,col)
            GameType.PLAYER_VS_AI -> {
                playUser(row,col)
                // if first player wins, ai shouldn't make a move
                if(
                    !gameState.isWinState(Cell(content = settings.firstPlayer.player.cellType)) ||
                        !gameState.isStandOff())
                    CoroutineScope(Main).launch {
                        delay(1000)
                        playAgent()
                    }
            }
        }

        // unlock game screen
        fragment?.let { unlockUserScreen(it) }
    }

    override fun checkForWinner() : PlayerData?{
        var gameState : GameState = settings.gameState

        when{
            // first player wins
            gameState.isWinState(Cell(content = settings.firstPlayer.player.cellType)) -> {
                inflateWinner(settings.firstPlayer)
                return settings.firstPlayer}
            // second player wins
            gameState.isWinState(Cell(content = settings.secondPlayer.player.cellType)) -> {
                inflateWinner(settings.secondPlayer)
                return settings.secondPlayer
            }
            // standoff
            gameState.isStandOff() -> {
                return null
            }
        }
        // there are still moves to make
        return null
    }

    override fun setFragment(fragment: Any) {
        this.fragment = when(fragment){
            is IEntryView -> fragment
            is IGameScreenView -> fragment
            is ISettingsView -> fragment
            else -> throw IllegalArgumentException()
        }
    }

    override fun setGameType(gameType: String) {
        settings.typeGame = when(gameType){
            GameType.PLAYER_VS_PLAYER.name -> GameType.PLAYER_VS_PLAYER
            GameType.PLAYER_VS_AI.name -> GameType.PLAYER_VS_AI
            else -> {
                // default game type
                GameType.PLAYER_VS_PLAYER
            }
        }
    }

    // updates the game state based on settings
    override fun createGameBasedOnTypeGame() {
        // create players based on game type and agent difficulty
        when(this.settings.typeGame){
            GameType.PLAYER_VS_PLAYER -> {
                settings.secondPlayer = UserData(
                    player = User(cellType = CellType.CIRCLE),
                    cellTypeImg = CellTypeImg.CIRCLE_BLACK,
                    winCellTypeImg = CellTypeImg.CIRCLE_RED
                )
            }
            GameType.PLAYER_VS_AI -> {
                settings.secondPlayer.player = Agent.createAgent(
                    cellType = CellType.CIRCLE,
                    diff = settings.difficulty
                ) as Player
            }
        }
        settings.gameState = GameState(
            grid = Grid(3),
            listOfPlayers = mutableListOf(this.settings.firstPlayer.player, this.settings.secondPlayer.player),
            notVisitedCell = Cell(),
            listOfMoves = mutableListOf()
        )

        settings.listOfActions = mutableListOf()

        // clear the Img grid
        settings.gridLayoutImgId = Array(3){IntArray(3)}
    }

    override fun inflateWinner(winPlayer: PlayerData) {
        if(fragment is IGameScreenView){
            val gameScreenFragment = fragment as IGameScreenView
            gameScreenFragment.apply {
                setCellImg(
                    settings.gameState.listOfWinMoves[0].first,
                    settings.gameState.listOfWinMoves[0].second,
                    winPlayer.winCellTypeImg.id)
                setCellImg(
                    settings.gameState.listOfWinMoves[1].first,
                    settings.gameState.listOfWinMoves[1].second,
                    winPlayer.winCellTypeImg.id)
                setCellImg(
                    settings.gameState.listOfWinMoves[2].first,
                    settings.gameState.listOfWinMoves[2].second,
                    winPlayer.winCellTypeImg.id)
            }
        } else throw IllegalArgumentException()
    }

    override fun lockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(LOCK)
        }
    }

    override fun unlockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(UNLOCK)
        }
    }

    override fun playAgent() {
        var gameState : GameState = settings.gameState

        val gameScreenFragment = fragment as IGameScreenView

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

        // set cell img in the grid layout
        this.settings.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        settings.listOfActions.add(gameState)


        when(checkForWinner()){
            settings.firstPlayer -> {
                // unlock game screen
                fragment?.let { unlockUserScreen(it) }

                gameScreenFragment.navigateToStart(FIRST_PLAYER_WIN)
            }
            settings.secondPlayer -> {
                // unlock game screen
                fragment?.let { unlockUserScreen(it) }
                gameScreenFragment.navigateToStart(SECOND_PLAYER_WINS)
            }
            else -> {
                if (gameState.isStandOff()) {
                    // unlock game screen
                    fragment?.let { unlockUserScreen(it) }

                    gameScreenFragment.navigateToStart(STANDOFF)
                }
            }
        }
    }

    override fun playUser(row: Int, col: Int) {

        var gameState : GameState = settings.gameState
        val gameScreenFragment = fragment as IGameScreenView
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

        // set cell img in the grid layout
        this.settings.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        settings.listOfActions.add(gameState)


        when(checkForWinner()){
            settings.firstPlayer -> {
                // unlock game screen
                fragment?.let { unlockUserScreen(it) }

                gameScreenFragment.navigateToStart(FIRST_PLAYER_WIN)
            }
            settings.secondPlayer -> {
                // unlock game screen
                fragment?.let { unlockUserScreen(it) }

                gameScreenFragment.navigateToStart(SECOND_PLAYER_WINS)
            }
            else -> {
                if (gameState.isStandOff()) {
                    // unlock game screen
                    fragment?.let { unlockUserScreen(it) }

                    gameScreenFragment.navigateToStart(STANDOFF)
                }
            }
        }
    }


    private fun checkForWinnerAndNavigateToStart(){
        if(fragment is IGameScreenView) {
            val gameScreenFragment = fragment as IGameScreenView
            var gameState : GameState = settings.gameState
            when (checkForWinner()) {
                settings.firstPlayer -> {
                    // unlock game screen
                    fragment?.let { unlockUserScreen(it) }

                    gameScreenFragment.navigateToStart(FIRST_PLAYER_WIN)
                }
                settings.secondPlayer -> {
                    // unlock game screen
                    fragment?.let { unlockUserScreen(it) }

                    gameScreenFragment.navigateToStart(SECOND_PLAYER_WINS)
                }
                else -> {
                    if (gameState.isStandOff()) {
                        // unlock game screen
                        fragment?.let { unlockUserScreen(it) }

                        gameScreenFragment.navigateToStart(STANDOFF)
                    }
                }
            }
        }
        else{
            throw IllegalArgumentException()
        }
    }
}