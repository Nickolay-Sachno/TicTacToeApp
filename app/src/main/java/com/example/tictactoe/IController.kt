package com.example.tictactoe

import GameState
import Player
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.PlayerData
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.SettingsFragment

interface IController {

    fun onGridCellSelected(row : Int, col : Int)
    fun playUser(row: Int, col: Int)
    fun playAgent()
    fun checkForWinner() : PlayerData?
    fun setFragment(fragment: Any)
    fun setGameType(gameType: String)
    fun createGameBasedOnTypeGame()
    fun inflateWinner(firstPlayer: PlayerData)
    fun lockUserScreen(view: View)
    fun unlockUserScreen(view: View)
}