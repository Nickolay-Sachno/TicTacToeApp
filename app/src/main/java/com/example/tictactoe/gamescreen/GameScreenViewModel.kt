package com.example.tictactoe.gamescreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tictactoe.Controller
import com.example.tictactoe.entry.WelcomeScreenViewModel
import com.example.tictactoe.enum.GameType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// delays
private const val AGENT_DELAY_MOVE_TIME: Long = 1000

class GameScreenViewModel : ViewModel() {

    private val LOCK: String = "LOCK"
    private val UNLOCK: String = "UNLOCK"
    private var board: Array<IntArray> = Array(3){IntArray(3)}
    lateinit var welcomeScreenViewModel: WelcomeScreenViewModel

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

    fun currentTurnImgLiveData() : LiveData<Int> = currentTurnImgMutableLiveData
    fun gridLiveData() : LiveData<Triple<Int, Int, Int>> = gridMutableLiveData
    fun isProgressBarVisibleLiveData() : LiveData<Boolean> = isProgressBarVisibleMutableLiveData
    fun isGameScreenBlockedLiveData() : LiveData<String> = fragmentLockStatusMutableLiveData
    fun boardImgLiveData() : LiveData<Array<IntArray>> = boardImgMutableLiveData


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
                    return
                }
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
                        return
                    }
                }

                // Agent turn
                fragmentLockStatusMutableLiveData.postValue(LOCK)
                CoroutineScope(Dispatchers.Main).launch {

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

}