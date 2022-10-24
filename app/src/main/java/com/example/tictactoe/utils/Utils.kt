package com.example.tictactoe.utils

import com.example.tictactoe.settings.GameStateBridge
import com.google.gson.Gson

object Utils {
    fun fromStringToGameStateBridge(str: String): GameStateBridge {
        return Gson().fromJson(str, GameStateBridge::class.java)
    }

    fun fromGameStateBridgeToString(gameStateBridge: GameStateBridge): String {
        return Gson().toJson(gameStateBridge)
    }
}