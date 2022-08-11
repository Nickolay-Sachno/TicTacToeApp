package com.example.tictactoe.settings

import AgentDifficulties
import com.example.tictactoe.View

interface ISettingsView : View {

    fun getDiff() : AgentDifficulties
    fun setDiff(diff : AgentDifficulties)
}