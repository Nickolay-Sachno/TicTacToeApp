package com.example.tictactoe.gamescreen

import GameState
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    lateinit var database: GameStateDatabaseDao
    var gameScreenUIState: GameScreenUIState = GameScreenUIState(
        currentTurnImg = Controller.controllerData.currentTurnImg,
        board = Board(Controller.controllerData.gridLayoutImgId),
        progressBarVisibility = View.INVISIBLE,
        lockScreen = false
    )

    fun inflateGameScreenFragment(){
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = Controller.currentTurnImg,
            board = Board(Controller.controllerData.gridLayoutImgId),
            progressBarVisibility = View.INVISIBLE,
            lockScreen = false
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    val gameScreenUIStateMutableData: MutableLiveData<GameScreenUIState> by lazy {
        MutableLiveData<GameScreenUIState>()
    }


    fun onCellClicked(row: Int, col: Int) {
        when (Controller.controllerData.gameType) {
            GameType.PLAYER_VS_PLAYER -> playerVsPlayerGame(col, row)
            GameType.PLAYER_VS_AI -> playerVsAiGame(col, row)
        }
    }

    private fun playerVsAiGame(col: Int, row: Int) {

        // Player turn
        updateLockScreen(true)
        Controller.playUser(row, col)

        // update the UI
        updateCellInBoard(
            Controller.controllerData.playerPlayedMove[0],
            Controller.controllerData.playerPlayedMove[1],
            Controller.controllerData.playerPlayedMove[2]
        )
        updateCurrentTurnImg(Controller.controllerData.currentTurnImg)

        // Prep Game State For Database and insert it
        viewModelScope.launch {
            val gameStateBridge: GameStateBridge = GameStateBridge(
                grid = GridBridge(
                    dim = 3,
                    matrix = kotlin.run {
                        List(3) { row ->
                            List(3) { col ->
                                CellBridge(
                                    row,
                                    col,
                                    Controller.controllerData.gameState.grid.matrix[row][col].content
                                )
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
            database.insert(
                GameStateData(
                    currentTurn = Controller.controllerData.gameState.currentTurn().cellType.name,
                    gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                )
            )
        }

        // if first player wins, ai shouldn't make a move
        if (Controller.checkIfPlayerWinAgent()) {
            // winner state
            if (Controller.controllerData.winnerState.size != 0) {
                updateCellInBoard(
                    Controller.controllerData.winnerState[0].first,
                    Controller.controllerData.winnerState[0].second,
                    Controller.controllerData.winnerState[0].third
                )
                updateCellInBoard(
                    Controller.controllerData.winnerState[1].first,
                    Controller.controllerData.winnerState[1].second,
                    Controller.controllerData.winnerState[0].third
                )
                updateCellInBoard(
                    Controller.controllerData.winnerState[2].first,
                    Controller.controllerData.winnerState[2].second,
                    Controller.controllerData.winnerState[0].third
                )
                Controller.clearControllerData()

                viewModelScope.launch {
                    database.clear()
                }
                return
            }
        }

        // Agent turn
        updateLockScreen(true)
        viewModelScope.launch {

            updateProgressBarVisibility(View.VISIBLE)

            // wait second
            delay(AGENT_DELAY_MOVE_TIME)

            // agent make a move
            Controller.playAgent()
            updateCellInBoard(Controller.controllerData.agentPlayedMove[0], Controller.controllerData.agentPlayedMove[1], Controller.controllerData.agentPlayedMove[2])

            updateProgressBarVisibility(View.INVISIBLE)
        }
        Controller.updateAgentPlayedMove(arrayListOf())
        updateCurrentTurnImg(Controller.controllerData.currentTurnImg)

        // Prep Game State For Database and insert it
        CoroutineScope(Job()).launch {
            val gameStateBridge = GameStateBridge(
                grid = GridBridge(
                    dim = 3,
                    matrix = kotlin.run {
                        List(3) { row ->
                            List(3) { col ->
                                CellBridge(
                                    row,
                                    col,
                                    Controller.controllerData.gameState.grid.matrix[row][col].content
                                )
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
            database.insert(
                GameStateData(
                    currentTurn = Controller.controllerData.gameState.currentTurn().cellType.chr.toString(),
                    gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                )
            )
        }
    }

    private fun playerVsPlayerGame(row: Int, col: Int) {

        // Player turn
        updateLockScreen(true)
        Controller.playUser(row, col)
        // update the board
        updateCellInBoard(
            Controller.controllerData.playerPlayedMove[0],
            Controller.controllerData.playerPlayedMove[1],
            Controller.controllerData.playerPlayedMove[2]
        )
        updateCurrentTurnImg(Controller.controllerData.currentTurnImg)
        updateLockScreen(false)
        // winner state
        if (Controller.controllerData.winnerState.size != 0) {
            updateCellInBoard(
                Controller.controllerData.winnerState[0].first,
                Controller.controllerData.winnerState[0].second,
                Controller.controllerData.winnerState[0].third
            )
            updateCellInBoard(
                Controller.controllerData.winnerState[1].first,
                Controller.controllerData.winnerState[1].second,
                Controller.controllerData.winnerState[0].third
            )
            updateCellInBoard(
                Controller.controllerData.winnerState[2].first,
                Controller.controllerData.winnerState[2].second,
                Controller.controllerData.winnerState[0].third
            )
            Controller.clearControllerData()

            viewModelScope.launch {
                database.clear()
            }
            return
        }
        // Prep Game State For Database and insert it
        viewModelScope.launch {
            val gameStateBridge: GameStateBridge = GameStateBridge(
                grid = GridBridge(
                    dim = 3,
                    matrix = kotlin.run {
                        List(3) { row ->
                            List(3) { col ->
                                CellBridge(
                                    row,
                                    col,
                                    Controller.controllerData.gameState.grid.matrix[row][col].content
                                )
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
            database.insert(
                GameStateData(
                    currentTurn = Controller.controllerData.gameState.currentTurn().cellType.name,
                    gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
                )
            )

            Log.i(
                "MODEL",
                "Inserted to DB:\n${Controller.controllerData.gameState.currentTurn().cellType.name}" +
                        "\n${Controller.fromGameStateBridgeToString(gameStateBridge)}"
            )
        }
    }


    /** Updating Controller Data */
    fun updateControllerGameState(gameState: GameState) {
        Controller.updateGameState(gameState)
    }


    /** Updating Game Screen UI State */
    fun updateCurrentTurnImg(currentTurnImg: Int) {
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateCellInBoard(row: Int, col: Int, imgId: Int) {
        val board = Board(
            boardImg = kotlin.run {
                Array(3) { r ->
                    Array(3) { c ->
                        if (r == row && c == col) {
                            imgId
                        } else {
                            gameScreenUIState.board.boardImg[r][c]
                        }
                    }
                }
            }
        )
        updateBoard(board)
    }

    fun updateBoard(board: Board) {
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = board,
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateProgressBarVisibility(visibility: Int) {
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = visibility,
            lockScreen = gameScreenUIState.lockScreen
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateLockScreen(lockScreen: Boolean) {
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = lockScreen
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateShowSuggestMove(state: Boolean){
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen,
            showSuggestMove = state,
            suggestMoveCoordinates = gameScreenUIState.suggestMoveCoordinates
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateSuggestMoveCoordinates(row: Int, col: Int){
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen,
            showSuggestMove = gameScreenUIState.showSuggestMove,
            suggestMoveCoordinates = listOf(row, col)
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    /** UI Data Classes */
    data class GameScreenUIState(
        val currentTurnImg: Int = CellTypeImg.CROSS_BLACK.id,
        val board: Board = Board(),
        val progressBarVisibility: Int = View.INVISIBLE,
        val lockScreen: Boolean = false,
        val showSuggestMove: Boolean = false,
        val suggestMoveCoordinates: List<Int> = listOf()
    )

    data class Board(
        val boardImg: Array<Array<Int>> = kotlin.run {
            Array(3) {
                Array(3) {
                    0
                }
            }
        }
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