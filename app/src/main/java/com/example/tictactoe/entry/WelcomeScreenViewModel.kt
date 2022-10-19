package com.example.tictactoe.entry

import androidx.lifecycle.ViewModel
import com.example.tictactoe.database.GameStateData
import com.example.tictactoe.database.GameStateDatabaseDao

class WelcomeScreenViewModel(
    private val database: GameStateDatabaseDao
) : ViewModel() {

    private val gameStateHistory : List<GameStateData> = database.getAllGameStates()

    fun getGameStateHistory() : List<GameStateData> {
        return gameStateHistory
    }

}