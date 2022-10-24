package com.example.tictactoe.settings

import AgentDifficulties
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
}
/** UI State : All variables must have default value */
data class SettingsUiState(
    val difficultyVisibility: Boolean = true,
    val switcherDifficulties: Boolean = true,
    )

data class SettingsData(
    val agentDifficulty: AgentDifficulties = AgentDifficulties.MEDIUM
)