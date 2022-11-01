package com.example.tictactoe.settings

import AgentDifficulties
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tictactoe.Controller

class SettingsViewModel : ViewModel() {
    private var settingsData: SettingsData = SettingsData()
    var settingsUIState: SettingsUiState = SettingsUiState()

    val settingsUIStateMutableData: MutableLiveData<SettingsUiState> by lazy {
        MutableLiveData<SettingsUiState>()
    }

    fun onClickSetAgentDifficulty(state: Boolean){
        settingsData = SettingsData(
            agentDifficulty = when(state){
                true -> AgentDifficulties.MEDIUM
                false -> AgentDifficulties.EASY
            }
        )
        updatingControllerSettingsData(settingsData)
        updateSettingsData(settingsData)
    }

    private fun updatingControllerSettingsData(settingsData: SettingsData) {
        Controller.updateSettingsData(settingsData)
    }

    fun inflateUIState() {
        settingsUIState = SettingsUiState(
            difficultyVisibility = true,
            switcherDifficulties = getDifficultyFromController()
        )
        settingsUIStateMutableData.postValue(settingsUIState)
    }

    private fun getDifficultyFromController(): Boolean {
        // controls the switch of MINIMAX algo of the agent, e.g. true -> Agent uses minimax, false -> Agent uses random move decision
        return when(Controller.controllerData.settings.agentDifficulty){
            AgentDifficulties.EASY -> false
            AgentDifficulties.MEDIUM -> true
        }
    }

    fun updateSettingsData(settingsData: SettingsData){
        settingsUIState = SettingsUiState(
            difficultyVisibility = settingsUIState.difficultyVisibility,
            switcherDifficulties = settingsUIState.switcherDifficulties,
            settingsData = settingsData
        )
        settingsUIStateMutableData.postValue(settingsUIState)
    }
}

/** UI State : All variables must have default value */
data class SettingsUiState(
    val difficultyVisibility: Boolean = true,
    val switcherDifficulties: Boolean = true,
    val settingsData: SettingsData = SettingsData()
)

data class SettingsData(
    val agentDifficulty: AgentDifficulties = AgentDifficulties.MEDIUM
)
