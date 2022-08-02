package com.example.tictactoe.settings

import AgentDifficulties
import Cell
import GameState
import Grid

data class Settings(
    // User visible
    val difficulty : AgentDifficulties = AgentDifficulties.MEDIUM,


    // User invisible
    val typeGame : String = "playerVsAi",
    val firstPlayer : UserData = UserData(),
    val secondPlayer : AgentData = AgentData(),
    val gameState: GameState = GameState(
        grid = Grid(3),
        listOfPlayers = mutableListOf(firstPlayer.player, secondPlayer.player),
        notVisitedCell = Cell(),
        listOfMoves = mutableListOf()
    )
)
