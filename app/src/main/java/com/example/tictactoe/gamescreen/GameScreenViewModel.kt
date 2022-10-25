package com.example.tictactoe.gamescreen

import GameState
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tictactoe.Controller
import com.example.tictactoe.database.GameStateData
import com.example.tictactoe.database.GameStateDatabaseDao
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


// delays
private const val AGENT_DELAY_MOVE_TIME: Long = 1000

class GameScreenViewModel() : ViewModel() {

    private val LOCK: String = "LOCK"
    private val UNLOCK: String = "UNLOCK"
    var board: Array<IntArray> = Array(3){IntArray(3)}
    lateinit var database: GameStateDatabaseDao
    val gameScreenUIState: GameScreenUIState = GameScreenUIState()

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

    val gameScreenUIStateMutableData: MutableLiveData<GameScreenUIState> by lazy{
        MutableLiveData<GameScreenUIState>()
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


    fun setCurrentTurnImg(imgId: Int){
        currentTurnImgMutableLiveData.postValue(imgId)
    }

    fun onCellClicked(row: Int, col: Int) {
        Controller.onCellSelected(row, col)
        when(Controller.controllerData.gameType){
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
                // Prep Game State For Database and insert it
                CoroutineScope(Job()).launch {
                    val gameStateBridge: GameStateBridge = GameStateBridge(
                        grid = GridBridge(
                            dim = 3,
                            matrix = kotlin.run {
                                List(3) { row ->
                                    List(3) { col ->
                                        CellBridge(row, col, Controller.controllerData.gameState.grid.matrix[row][col].content)
                                    }
                                }
                            }
                        ),
                        notVisitedCell = CellBridge(content = Controller.controllerData.gameState.notVisitedCell.content),
                        listOfPlayers = mutableListOf<PlayerBridge>().apply {
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[0].cellType))
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[1].cellType))
                        },
                        listOfMoves = Controller.controllerData.gameState.listOfMoves
                    )
                    database.insert(GameStateData(
                        currentTurn = Controller.controllerData.gameState.currentTurn().cellType.name,
                        gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                    ))

                    Log.i("MODEL", "Inserted to DB:\n${Controller.controllerData.gameState.currentTurn().cellType.name}" +
                            "\n${Controller.fromGameStateBridgeToString(gameStateBridge)}")
                }

                insertDatabaseLiveData("Inserted:\ncurrent turn: ${
                    Controller.controllerData.gameState.currentTurn().cellType.chr
                }\nboard:\n${Controller.controllerData.gameState}")
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

                // Prep Game State For Database and insert it
                CoroutineScope(Job()).launch {
                    val gameStateBridge: GameStateBridge = GameStateBridge(
                        grid = GridBridge(
                            dim = 3,
                            matrix = kotlin.run {
                                List(3) { row ->
                                    List(3) { col ->
                                        CellBridge(row, col, Controller.controllerData.gameState.grid.matrix[row][col].content)
                                    }
                                }
                            }
                        ),
                        notVisitedCell = CellBridge(content = Controller.controllerData.gameState.notVisitedCell.content),
                        listOfPlayers = mutableListOf<PlayerBridge>().apply {
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[0].cellType))
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[1].cellType))
                        },
                        listOfMoves = Controller.controllerData.gameState.listOfMoves
                    )
                    database.insert(GameStateData(
                        currentTurn = Controller.controllerData.gameState.currentTurn().cellType.name,
                        gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                    ))
                }

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

                // Prep Game State For Database and insert it
                CoroutineScope(Job()).launch {
                    val gameStateBridge: GameStateBridge = GameStateBridge(
                        grid = GridBridge(
                            dim = 3,
                            matrix = kotlin.run {
                                List(3) { row ->
                                    List(3) { col ->
                                        CellBridge(row, col, Controller.controllerData.gameState.grid.matrix[row][col].content)
                                    }
                                }
                            }
                        ),
                        notVisitedCell = CellBridge(content = Controller.controllerData.gameState.notVisitedCell.content),
                        listOfPlayers = mutableListOf<PlayerBridge>().apply {
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[0].cellType))
                            add(PlayerBridge(Controller.controllerData.gameState.listOfPlayers[1].cellType))
                        },
                        listOfMoves = Controller.controllerData.gameState.listOfMoves
                    )
                    database.insert(GameStateData(
                        currentTurn = Controller.controllerData.gameState.currentTurn().cellType.chr.toString(),
                        gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                    ))
                }
            }
        }
    }

    fun updateBoardImg() {
        boardImgMutableLiveData.postValue(board)
        fragmentLockStatusMutableLiveData.postValue(UNLOCK)
    }

    fun updateControllerGameState(gameState: GameState) {
        Controller.updateGameState(gameState)
    }

    fun updateListOfActions(){
        Log.i("VIEW MODEL", "Update List of Actions() ")
        val listOfActions: MutableList<GameState> = mutableListOf()

        CoroutineScope(Job()).launch{
            database.getAllGameStates().forEach{gameStateData ->
                listOfActions.add(Controller.fromStringToGameStateBridge(gameStateData.gameState).getGameStateBridgeFromGameState().deepCopy())
            }

            Controller.updateListOfActions(listOfActions)
        }
    }

    data class GameScreenUIState(
        val currentTurnImg: Int = CellTypeImg.CROSS_BLACK.id,
        val boardImg: Board = Board(),
        val progressBarVisibility: Int = View.INVISIBLE,
        val lockScreen: Boolean = false
    )

    data class Board(
        val boardImg: Array<IntArray> = Array(3){IntArray(3)}
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Board

            if (!boardImg.contentDeepEquals(other.boardImg)) return false

            return true
        }

        override fun hashCode(): Int {
            return boardImg.contentDeepHashCode()
        }
    }

}