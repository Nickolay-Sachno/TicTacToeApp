package com.example.tictactoe.repository

import com.example.tictactoe.database.GameStateData
import com.example.tictactoe.database.GameStateDatabaseDao

class GameStateDatabaseRepository(private val database: GameStateDatabaseDao) {

    suspend fun insert(gameStateData: GameStateData) {
        database.insert(gameStateData)
    }

    suspend fun update(gameStateData: GameStateData) {
        database.update(gameStateData)
    }

    suspend fun get(key: Long): GameStateData {
        return database.get(key)
    }

    suspend fun clear() {
        database.clear()
    }

    suspend fun getAllGameStates(): List<GameStateData> {
        return database.getAllGameStates()
    }

    suspend fun getLatestGameState(): GameStateData {
        return database.getLatestGameState()
    }
}