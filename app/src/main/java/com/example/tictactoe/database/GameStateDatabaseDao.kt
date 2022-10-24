package com.example.tictactoe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GameStateDatabaseDao {

    @Insert
    fun insert(gameStateData: GameStateData)

    @Update
    fun update(gameStateData: GameStateData)

    @Query("SELECT * from game_state_table WHERE gameStateId = :key")
    fun get(key: Long): GameStateData

    @Query("DELETE FROM game_state_table")
    fun clear()

    @Query("SELECT * FROM game_state_table ORDER BY gameStateId ASC")
    fun getAllGameStates(): List<GameStateData>

    @Query("SELECT * FROM game_state_table ORDER BY gameStateId DESC LIMIT 1")
    fun getLatestGameState(): GameStateData
}