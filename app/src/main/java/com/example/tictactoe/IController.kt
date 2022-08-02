package com.example.tictactoe

import GameState
import Player
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.SettingsFragment

interface IController {

    var gameState : GameState

    fun play(gameType : String)
    fun getPlayer() : Player
    fun setPlayer(settingsFragment: SettingsFragment? = null)
    fun getCellTypeImg() : Int
    fun onGridCellSelected(row : Int, col : Int)
}