package com.example.tictactoe.entry

import GameState
import android.view.View
import androidx.lifecycle.*
import com.example.tictactoe.Controller
import com.example.tictactoe.database.GameStateDatabaseDao
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.utils.Utils
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class WelcomeScreenViewModel() : ViewModel() {

    lateinit var database: GameStateDatabaseDao
    var welcomeScreenUiState: WelcomeScreenUiState = WelcomeScreenUiState()

    val welcomeScreenUiStateMutableLiveData: MutableLiveData<WelcomeScreenUiState> by lazy {
        MutableLiveData<WelcomeScreenUiState>()
    }

    /** Called every onCreate() in the fragment */
    fun inflateWelcomeScreenFragment() {
        viewModelScope.launch {
            if (database.getAllGameStates().isNotEmpty()) {
                welcomeScreenUiStateMutableLiveData.postValue(
                    WelcomeScreenUiState(
                        restoreGameVisibility = View.VISIBLE
                    )
                )
            } else {
                welcomeScreenUiStateMutableLiveData.postValue(
                    WelcomeScreenUiState() // default value
                )
            }
        }
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
        viewModelScope.launch {
            restoreGameStateFromDatabase()
        }
    }

    private suspend fun restoreGameStateFromDatabase() {
        // hide restore button
        updateRestoreButtonVisibility(View.GONE)
        // show loading text and progress bar
        updateProgressBarVisibility(View.VISIBLE)
        updateRestoreProgressBarTextVisibility(View.VISIBLE)

        try {
            val gameState = getGameStateFromDatabase()
            val gameStateCurrentTurnImg = getCurrentTurnIngFromDatabase()
            Controller.apply {
                updateCurrentTurnImg(gameStateCurrentTurnImg)
                updateGameState(gameState)
                updateGridLayoutImgId(
                    kotlin.run {
                        Array(3) { row ->
                            Array(3) { col ->
                                when (gameState.grid.matrix[row][col].content) {
                                    CellType.CROSS -> CellTypeImg.CROSS_BLACK.id
                                    CellType.CIRCLE -> CellTypeImg.CIRCLE_BLACK.id
                                    else -> 0
                                }
                            }
                        }
                    }
                )
            }

        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        } finally {
            // reverse
            updateRestoreProgressBarTextVisibility(View.GONE)
            updateProgressBarVisibility(View.GONE)
            updateRestoreButtonVisibility(View.VISIBLE)
        }
    }

    private suspend fun getCurrentTurnIngFromDatabase(): Int {
        var gameStateCurrentTurnImg = 0
        viewModelScope.launch {
            gameStateCurrentTurnImg = when (database.getLatestGameState().currentTurn) {
                CellType.CROSS.name -> CellTypeImg.CROSS_BLACK.id
                CellType.CIRCLE.name -> CellTypeImg.CIRCLE_BLACK.id
                else -> throw IllegalArgumentException()
            }
        }.join()
        return gameStateCurrentTurnImg
    }

    private suspend fun getGameStateFromDatabase(): GameState {
        var gameState: GameState = Controller.controllerData.gameState
        viewModelScope.launch {
            val gameStateBridgeString = database.getLatestGameState().gameState
            val gameStateBridge = Utils.fromStringToGameStateBridge(gameStateBridgeString)
            gameState = gameStateBridge.getGameStateBridgeFromGameState()
        }.join()

        return gameState
    }


    private fun clearDatabase() {
        viewModelScope.launch {
            database.clear()
        }
    }

    private fun updateControllerAndBuildGame(gameType: GameType) {
        when (gameType) {
            GameType.PLAYER_VS_PLAYER -> Controller.updateGameType(GameType.PLAYER_VS_PLAYER)
            GameType.PLAYER_VS_AI -> Controller.updateGameType(GameType.PLAYER_VS_AI)
        }
        Controller.createGameBasedOnTypeGame()
    }

    /** Update UI State functions */
    fun updateProgressBarVisibility(visibility: Int) {
        welcomeScreenUiState = WelcomeScreenUiState(
            playerVsAiVisibility = welcomeScreenUiState.playerVsAiVisibility,
            playerVsPlayerVisibility = welcomeScreenUiState.playerVsPlayerVisibility,
            restoreGameVisibility = welcomeScreenUiState.restoreGameVisibility,
            settingsVisibility = welcomeScreenUiState.settingsVisibility,
            progressBarVisibility = visibility,
            restoreGameProgressBarTextVisibility = welcomeScreenUiState.progressBarVisibility
        )
        welcomeScreenUiStateMutableLiveData.postValue(
            welcomeScreenUiState
        )
    }

    fun updateRestoreProgressBarTextVisibility(visibility: Int) {
        welcomeScreenUiState = WelcomeScreenUiState(
            playerVsAiVisibility = welcomeScreenUiState.playerVsAiVisibility,
            playerVsPlayerVisibility = welcomeScreenUiState.playerVsPlayerVisibility,
            restoreGameVisibility = welcomeScreenUiState.restoreGameVisibility,
            settingsVisibility = welcomeScreenUiState.settingsVisibility,
            progressBarVisibility = welcomeScreenUiState.progressBarVisibility,
            restoreGameProgressBarTextVisibility = visibility
        )
        welcomeScreenUiStateMutableLiveData.postValue(
            welcomeScreenUiState
        )
    }

    fun updateRestoreButtonVisibility(visibility: Int) {
        welcomeScreenUiState = WelcomeScreenUiState(
            playerVsAiVisibility = welcomeScreenUiState.playerVsAiVisibility,
            playerVsPlayerVisibility = welcomeScreenUiState.playerVsPlayerVisibility,
            restoreGameVisibility = visibility,
            settingsVisibility = welcomeScreenUiState.settingsVisibility,
            progressBarVisibility = welcomeScreenUiState.progressBarVisibility,
            restoreGameProgressBarTextVisibility = welcomeScreenUiState.restoreGameProgressBarTextVisibility
        )
        welcomeScreenUiStateMutableLiveData.postValue(
            welcomeScreenUiState
        )
    }
}

/** Welcome Screen UI State Data Class */
data class WelcomeScreenUiState(
    // buttons
    val playerVsPlayerVisibility: Int = View.VISIBLE,
    val playerVsAiVisibility: Int = View.VISIBLE,
    val restoreGameVisibility: Int = View.GONE,
    val settingsVisibility: Int = View.VISIBLE,

    // loading data from DB
    val progressBarVisibility: Int = View.GONE,
    val restoreGameProgressBarTextVisibility: Int = View.GONE
)

