package com.example.tictactoe

import GameState
import Player
import android.content.Context
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.PlayerData
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.SettingsFragment

interface IController {

    //fun onCellSelected(row : Int, col : Int)
    fun playUser(row: Int, col: Int)
    fun playAgent()
    fun checkForWinner() : PlayerData?
    fun setFragment(fragment: Any)
    fun setGameType(gameType: String)
    fun createGameBasedOnTypeGame()
    fun inflateWinner(winPlayer: PlayerData)
    fun lockUserScreen(view: View)
    fun unlockUserScreen(view: View)
    fun setFragmentProgressBarVisibility(view:View?, name: String)
    fun getNextMoveHelpFromApi(context: Context)
    fun isOnline(context: Context) : Boolean
    fun updateCurrentTurnImg(gameStateCurrentTurnImg: Int)
    fun updateGameState(gameState: GameState)
    fun updateGameType(playerVsPlayer: GameType)
    fun updateWinnerState(winnerState: ArrayList<Triple<Int, Int, Int>>)
    fun updateAgentPlayedMove(agentPlayedMove: ArrayList<Int>)
    fun clearControllerData()
}