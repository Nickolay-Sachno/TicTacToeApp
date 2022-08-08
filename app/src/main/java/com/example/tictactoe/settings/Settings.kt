package com.example.tictactoe.settings

import AgentDifficulties
import Cell
import GameState
import Grid
import Player
import com.example.tictactoe.enum.GameType
import java.io.Serializable

data class Settings(
    // User visible
    var difficulty : AgentDifficulties = AgentDifficulties.MEDIUM,


    // User invisible
    var typeGame: GameType = GameType.PLAYER_VS_AI,
    var firstPlayer : PlayerData = UserData(),
    var secondPlayer : PlayerData = AgentData(),
    var gameState: GameState = GameState(
        grid = Grid(3),
        listOfPlayers = mutableListOf(firstPlayer.player, secondPlayer.player),
        notVisitedCell = Cell(),
        listOfMoves = mutableListOf()
    )
) : Serializable
