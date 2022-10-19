package com.example.tictactoe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state_table")
data class GameStateData(
    @PrimaryKey(autoGenerate = true)
    var gameStateId: Long = 0L,

    @ColumnInfo(name = "current_turn")
    var currentTurn: String = "",

    @ColumnInfo(name = "board")
    var board: String = ""
)
