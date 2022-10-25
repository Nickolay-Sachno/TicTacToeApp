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
    var difficulty: AgentDifficulties = AgentDifficulties.MEDIUM
) : Serializable
