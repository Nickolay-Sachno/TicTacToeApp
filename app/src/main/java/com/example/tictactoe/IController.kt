package com.example.tictactoe

import GameState
import Player
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.SettingsFragment

interface IController {

    var gameState : GameState

    fun onGridCellSelected(row : Int, col : Int)
    fun playUser(row: Int, col: Int)
    fun playAgent()
    fun checkForWinner()
}