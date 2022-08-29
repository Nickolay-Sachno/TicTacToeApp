package com.example.tictactoe.networking

data class Result(
    val game: String,
    val player: String,
    val recommendation: Int,
    val strength: Int
)
