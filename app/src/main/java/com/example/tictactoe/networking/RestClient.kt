package com.example.tictactoe.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RestClient {

    private const val BASE_URL = "https://stujo-tic-tac-toe-stujo-v1.p.rapidapi.com"

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val movesService = retrofit.create(MovesService::class.java)

}