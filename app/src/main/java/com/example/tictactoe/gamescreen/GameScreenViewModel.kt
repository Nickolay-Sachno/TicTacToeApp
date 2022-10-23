package com.example.tictactoe.gamescreen

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictactoe.Controller
import com.example.tictactoe.database.GameStateData
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.database.GameStateDatabaseDao
import com.example.tictactoe.entry.WelcomeScreenViewModel
import com.example.tictactoe.enum.GameType
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


// delays
private const val AGENT_DELAY_MOVE_TIME: Long = 1000

class GameScreenViewModel(
) : ViewModel() {

    private val LOCK: String = "LOCK"
    private val UNLOCK: String = "UNLOCK"
    private var board: Array<IntArray> = Array(3){IntArray(3)}
    lateinit var database: GameStateDatabaseDao

//    private val gameStateKey: Long = 0L,

    private val currentTurnImgMutableLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int >()
    }

    private val gridMutableLiveData: MutableLiveData<Triple<Int, Int, Int>> by lazy {
        MutableLiveData<Triple<Int, Int, Int>>()
    }

    private val isProgressBarVisibleMutableLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val fragmentLockStatusMutableLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val boardImgMutableLiveData: MutableLiveData<Array<IntArray>> by lazy {
        MutableLiveData<Array<IntArray>>()
    }

    private val insertDatabaseMutableLiveData: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

    fun currentTurnImgLiveData() : LiveData<Int> = currentTurnImgMutableLiveData
    fun gridLiveData() : LiveData<Triple<Int, Int, Int>> = gridMutableLiveData
    fun isProgressBarVisibleLiveData() : LiveData<Boolean> = isProgressBarVisibleMutableLiveData
    fun isGameScreenBlockedLiveData() : LiveData<String> = fragmentLockStatusMutableLiveData
    fun boardImgLiveData() : LiveData<Array<IntArray>> = boardImgMutableLiveData
    fun insertDatabaseLiveData(): LiveData<String> = insertDatabaseMutableLiveData

    fun insertDatabaseLiveData(toast: String){
        insertDatabaseMutableLiveData.postValue(toast)
    }


    private fun setCurrentTurnImg(imgId: Int){
        currentTurnImgMutableLiveData.postValue(imgId)
    }

    fun onCellClicked(row: Int, col: Int) {
        Controller.onCellSelected(row, col)
        when(Controller.settings.typeGame){
            GameType.PLAYER_VS_PLAYER -> {
                // Player turn
                fragmentLockStatusMutableLiveData.postValue(LOCK)
                Controller.playUser(row,col)
                // update the board
                board[Controller.playerPlayedMove[0]][Controller.playerPlayedMove[1]] =  Controller.playerPlayedMove[2]
                Controller.playerPlayedMove = arrayListOf()
                // update the UI
                updateBoardImg()
                setCurrentTurnImg(Controller.currentTurnImg)
                // winner state
                if (Controller.winnerState.size != 0){
                    board[Controller.winnerState[0].first][Controller.winnerState[0].second] = Controller.winnerState[0].third
                    board[Controller.winnerState[1].first][Controller.winnerState[1].second] = Controller.winnerState[0].third
                    board[Controller.winnerState[2].first][Controller.winnerState[2].second] = Controller.winnerState[0].third
                    updateBoardImg()
                    Controller.winnerState = arrayListOf()

                    CoroutineScope(Job()).launch {
                        database.clear()
                    }
                    return
                }
                CoroutineScope(Job()).launch {
                    database.insert(GameStateData(
                        currentTurn = Controller.settings.gameState.currentTurn().cellType.chr.toString(),
                        board = Controller.settings.gameState.toString()
                    ))
                }

                insertDatabaseLiveData("Inserted:\ncurrent turn: ${
                    Controller.settings.gameState.currentTurn().cellType.chr
                }\nboard:\n${Controller.settings.gameState}")
            }
            GameType.PLAYER_VS_AI -> {
                // Player turn
                fragmentLockStatusMutableLiveData.postValue(LOCK)
                Controller.playUser(row,col)

                // update the board
                board[Controller.playerPlayedMove[0]][Controller.playerPlayedMove[1]] =  Controller.playerPlayedMove[2]
                Controller.playerPlayedMove = arrayListOf()

                // update the UI
                updateBoardImg()
                setCurrentTurnImg(Controller.currentTurnImg)

                // if first player wins, ai shouldn't make a move
                if(Controller.checkIfPlayerWinAgent()){
                    // winner state
                    if (Controller.winnerState.size != 0){
                        board[Controller.winnerState[0].first][Controller.winnerState[0].second] = Controller.winnerState[0].third
                        board[Controller.winnerState[1].first][Controller.winnerState[1].second] = Controller.winnerState[0].third
                        board[Controller.winnerState[2].first][Controller.winnerState[2].second] = Controller.winnerState[0].third
                        updateBoardImg()
                        Controller.winnerState = arrayListOf()

                        CoroutineScope(Job()).launch {
                            database.clear()
                        }
                        return
                    }
                }

                // Agent turn
                fragmentLockStatusMutableLiveData.postValue(LOCK)
                CoroutineScope(Main).launch {

                    isProgressBarVisibleMutableLiveData.postValue(true)

                    // wait second
                    delay(AGENT_DELAY_MOVE_TIME)

                    // agent make a move
                    Controller.playAgent()
                    board[Controller.agentPlayedMove[0]][Controller.agentPlayedMove[1]] =  Controller.agentPlayedMove[2]
                    // update the UI
                    updateBoardImg()

                    isProgressBarVisibleMutableLiveData.postValue(false)
                }
                Controller.agentPlayedMove = arrayListOf()
                setCurrentTurnImg(Controller.currentTurnImg)
            }
        }
    }

    private fun updateBoardImg() {
        boardImgMutableLiveData.postValue(board)
        fragmentLockStatusMutableLiveData.postValue(UNLOCK)
    }

//    fun onSaveGameState(gameState: String){
//        viewModelScope.launch {
//            val currentGameState = database.get(gameStateKey) ?: return@launch
//            currentGameState.board = Controller.getValidStringToApiCallFromGameState(Controller.settings.gameState.toString())
//            currentGameState.currentTurn = Controller.settings.gameState.currentTurn().cellType.chr.toString()
//
//            database.update(currentGameState)
//        }
//    }

}