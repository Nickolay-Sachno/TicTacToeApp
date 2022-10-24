package com.example.tictactoe.entry

import android.view.View
import androidx.lifecycle.*
import com.example.tictactoe.Controller
import com.example.tictactoe.database.GameStateDatabaseDao
import com.example.tictactoe.enum.GameType
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class WelcomeScreenViewModel() : ViewModel() {

    lateinit var database: GameStateDatabaseDao

    val welcomeScreenUiStateMutableLiveData: MutableLiveData<WelcomeScreenUiState> by lazy {
        MutableLiveData<WelcomeScreenUiState>()
    }

    fun playerVsPlayerClicked() {
        clearDatabase()
        updateControllerAndBuildGame(GameType.PLAYER_VS_PLAYER)
    }

    fun playerVsAiClicked() {
        clearDatabase()
        updateControllerAndBuildGame(GameType.PLAYER_VS_AI)
    }

    fun settingsClicked() {}
    fun restoreGameClicked() {

    }


    private fun clearDatabase() {
        viewModelScope.launch{
            database.clear()
        }
    }

    private fun updateControllerAndBuildGame(gameType: GameType) {
        when (gameType) {
            GameType.PLAYER_VS_PLAYER -> Controller.settings.typeGame = GameType.PLAYER_VS_PLAYER
            GameType.PLAYER_VS_AI -> Controller.settings.typeGame = GameType.PLAYER_VS_AI
        }
        Controller.createGameBasedOnTypeGame()
    }
}

data class WelcomeScreenUiState(
    // buttons
    val playerVsPlayerVisibility: Int = View.VISIBLE,
    val playerVsAiVisibility: Int = View.VISIBLE,
    val restoreGameVisibility: Int = View.GONE,
    val settingsVisibility: Int = View.VISIBLE

    //
)

