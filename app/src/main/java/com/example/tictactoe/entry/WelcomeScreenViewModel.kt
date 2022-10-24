package com.example.tictactoe.entry

import GameState
import android.view.View
import androidx.lifecycle.*
import com.example.tictactoe.Controller
import com.example.tictactoe.database.GameStateDatabaseDao
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import kotlin.coroutines.coroutineContext

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
        restoreGameStateFromDatabase()
    }

    private fun restoreGameStateFromDatabase() {
        try {
            val gameState = getGameStateFromDatabase()
            val gameStateCurrentTurnImg = getCurrentTurnIngFromDatabase()
            Controller.apply {
                updateCurrentTurnImg(gameStateCurrentTurnImg)
                updateGameState(gameState)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
    }

    private fun getCurrentTurnIngFromDatabase(): Int {
        var gameStateCurrentTurnImg: Int = 0
        var finishLoading: Boolean = false
        viewModelScope.launch {
            gameStateCurrentTurnImg =
                when (database.getLatestGameState().currentTurn) {
                    CellType.CROSS.name -> CellTypeImg.CROSS_BLACK.id
                    else -> CellTypeImg.CIRCLE_BLACK.id
                }
            finishLoading = true
        }

        while (!finishLoading) {
            CoroutineScope(Main).launch {
                delay(500)
            }
        }

        return gameStateCurrentTurnImg
    }

    private fun getGameStateFromDatabase(): GameState {
        var gameState: GameState = Controller.controllerData.gameState
        var finishLoading = false
        viewModelScope.launch {
            val gameStateBridgeString = database.getLatestGameState().gameState
            val gameStateBridge = Utils.fromStringToGameStateBridge(gameStateBridgeString)
            gameState = gameStateBridge.getGameStateBridgeFromGameState()
            finishLoading = true
        }

        // wait from the database to finish loading new GameState
        while (!finishLoading) {
            CoroutineScope(Main).launch {
                delay(500)
            }
        }

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
}

data class WelcomeScreenUiState(
    // buttons
    val playerVsPlayerVisibility: Int = View.VISIBLE,
    val playerVsAiVisibility: Int = View.VISIBLE,
    val restoreGameVisibility: Int = View.GONE,
    val settingsVisibility: Int = View.VISIBLE

    //
)

