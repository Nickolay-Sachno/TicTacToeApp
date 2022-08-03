package com.example.tictactoe.settings

import AgentDifficulties
import Cell
import GameState
import Grid
import Player
import java.io.Serializable

data class Settings(
    // User visible
    val difficulty : AgentDifficulties = AgentDifficulties.MEDIUM,


    // User invisible
    var typeGame : String = "playerVsAi",
    var firstPlayer : PlayerData = UserData(),
    var secondPlayer : PlayerData = AgentData(),
    val gameState: GameState = GameState(
        grid = Grid(3),
        listOfPlayers = mutableListOf(firstPlayer.player, secondPlayer.player),
        notVisitedCell = Cell(),
        listOfMoves = mutableListOf()
    )
) : Serializable
