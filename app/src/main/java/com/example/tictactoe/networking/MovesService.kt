package com.example.tictactoe.networking

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path


interface MovesService {

    // BaseUrl/game/turn
    // https://stujo-tic-tac-toe-stujo-v1.p.rapidapi.com/X-O-XO---/X
    @Headers(
        "X-RapidAPI-Key:513973383fmsh8a7b8638f3e7838p1ebf1ejsne72aa72cf888",
        "X-RapidAPI-Host:stujo-tic-tac-toe-stujo-v1.p.rapidapi.com")
    @GET("/{game}/{turn}")
    fun getNextMove(
        @Path("game") game: String,
        @Path("turn") turn: String
    ): Call<Result>

}