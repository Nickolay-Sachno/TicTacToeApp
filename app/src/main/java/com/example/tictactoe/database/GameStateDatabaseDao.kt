package com.example.tictactoe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GameStateDatabaseDao {

    @Insert
    suspend fun insert(gameStateData: GameStateData)

    @Update
    suspend fun update(gameStateData: GameStateData)

    @Query("SELECT * from game_state_table WHERE gameStateId = :key")
    suspend fun get(key: Long): GameStateData

    @Query("DELETE FROM game_state_table")
    suspend fun clear()

    @Query("SELECT * FROM game_state_table ORDER BY gameStateId ASC")
    suspend fun getAllGameStates(): List<GameStateData>

    @Query("SELECT * FROM game_state_table ORDER BY gameStateId DESC LIMIT 1")
    suspend fun getLatestGameState(): GameStateData
}