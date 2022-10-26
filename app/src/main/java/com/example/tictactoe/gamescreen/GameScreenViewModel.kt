package com.example.tictactoe.gamescreen

import Cell
import GameState
import Player
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.Toast
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
    private var gameScreenUIState: GameScreenUIState = GameScreenUIState(
        currentTurnImg = Controller.controllerData.currentTurnImg,
        board = Board(Controller.controllerData.gridLayoutImgId),
        progressBarVisibility = View.INVISIBLE,
        lockScreen = false
    )

    /** Called every onCreate() in the fragment */
    fun inflateGameScreenFragment() {
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = getLatestCurrentTurnImgFromController(),
            board = Board(getLatestGridLayoutImgFromController()),
            progressBarVisibility = View.INVISIBLE,
            lockScreen = false
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    val gameScreenUIStateMutableData: MutableLiveData<GameScreenUIState> by lazy {
        MutableLiveData<GameScreenUIState>()
    }


    fun onCellClicked(row: Int, col: Int) {
        when (getLatestGameTypeFromController()) {
            GameType.PLAYER_VS_PLAYER -> playerVsPlayerGame(row, col)
            GameType.PLAYER_VS_AI -> playerVsAiGame(row, col)
        }
    }

    fun onNextMoveBtnClicked(context: Context) {
        Controller.getNextMoveHelpFromApi(context)
        // lock the screen
        updateLockScreen(true)
        // check for internet connection
        if (hasInternetConnection(context)){
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            updateLockScreen(false)
            return
        }
        // start the animation of the progress bar with delay
        // get current board as string
        // get current turn as string
        // send those to the API and get answer
        // end the animation of the progress bar
        // animate the suggested move
        // unlock the screen
    }

    fun hasInternetConnection(context: Context) : Boolean{
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    private fun updateCellInBoardAfterPlayerPlayed() {
        updateCellInBoard(
            getLatestPlayerPlayedMoveFromController()[0],
            getLatestPlayerPlayedMoveFromController()[1],
            getLatestPlayerPlayedMoveFromController()[2]
        )
    }

    private fun updateCellInBoardAfterAiPlayed() {
        updateCellInBoard(
            getLatestAgentPlayedMoveFromController()[0],
            getLatestAgentPlayedMoveFromController()[1],
            getLatestAgentPlayedMoveFromController()[2]
        )
    }

    private fun playerVsAiGame(row: Int, col: Int) {

        // Player turn
        updateLockScreen(true)
        playUserFromController(row, col)

        // update the UI
        updateCellInBoardAfterPlayerPlayed()
        updateCurrentTurnImg(getLatestCurrentTurnImgFromController())

        // Prep Game State For Database and insert it
        viewModelScope.launch {
            prepareGameStateForDatabaseAndInsertIt()
        }

        // if first player wins, ai shouldn't make a move
        if (checkIfPlayedWinAiFromController()) {
            // winner state
            if (getLatestWinnerStateFromController().size != 0) {
                updateWinnerUIState()
                clearDataFromControllerData()

                viewModelScope.launch {
                    database.clear()
                }
                return
            }
        }

        // Agent turn
        updateLockScreen(true)
        CoroutineScope(Main).launch {

            updateProgressBarVisibility(View.VISIBLE)

            // wait second
            delay(AGENT_DELAY_MOVE_TIME)

            // agent make a move
            playAgentFromController()
            updateCellInBoardAfterAiPlayed()
            updateAgentMakeMoveToController()
            updateProgressBarVisibility(View.INVISIBLE)
        }
        updateCurrentTurnImg(getLatestCurrentTurnImgFromController())

        // Prep Game State For Database and insert it
        viewModelScope.launch {
            prepareGameStateForDatabaseAndInsertIt()
        }
    }

    private fun updateWinnerUIState() {
        updateCellInBoard(
            getLatestWinnerStateFromController()[0].first,
            getLatestWinnerStateFromController()[0].second,
            getLatestWinnerStateFromController()[0].third
        )
        updateCellInBoard(
            getLatestWinnerStateFromController()[1].first,
            getLatestWinnerStateFromController()[1].second,
            getLatestWinnerStateFromController()[0].third
        )
        updateCellInBoard(
            getLatestWinnerStateFromController()[2].first,
            getLatestWinnerStateFromController()[2].second,
            getLatestWinnerStateFromController()[0].third
        )
    }

    private fun playerVsPlayerGame(row: Int, col: Int) {

        // Player turn
        updateLockScreen(true)
        playUserFromController(col, row)
        // update the board
        updateCellInBoardAfterPlayerPlayed()
        updateCurrentTurnImg(getLatestCurrentTurnImgFromController())
        updateLockScreen(false)
        // winner state
        if (getLatestWinnerStateFromController().size != 0) {
            updateWinnerUIState()
            clearDataFromControllerData()

            viewModelScope.launch {
                database.clear()
            }
            return
        }
        // Prep Game State For Database and insert it
        viewModelScope.launch {
            prepareGameStateForDatabaseAndInsertIt()
        }
    }

    suspend fun prepareGameStateForDatabaseAndInsertIt() {
        val gameStateBridge = GameStateBridge(
            grid = getLatestGridBridgeFromController(),
            notVisitedCell = getLatestCellBridgeFromController(),
            listOfPlayers = getLatestPlayerBridgeListFromController(),
            listOfMoves = getLatestListOfMovesFromController()
        )
        insertDataToDatabase(gameStateBridge)
    }

    suspend fun insertDataToDatabase(gameStateBridge: GameStateBridge) {
        database.insert(
            GameStateData(
                currentTurn = getLatestCurrentTurnFromController(),
                gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
            )
        )
    }


    /** ***********************************************************************************************************************/
    /** ********************************************** Get/Set data from/to Controller ****************************************/
    /** ***********************************************************************************************************************/

    private fun playUserFromController(row: Int, col: Int) {
        Controller.playUser(row, col)
    }

    private fun playAgentFromController() {
        Controller.playAgent()
    }

    private fun updateAgentMakeMoveToController() {
        Controller.updateAgentPlayedMove(arrayListOf())
    }

    private fun clearDataFromControllerData() {
        Controller.clearControllerData()
    }

    private fun checkIfPlayedWinAiFromController(): Boolean {
        return Controller.checkIfPlayerWinAgent()
    }

    private fun getLatestGridLayoutImgFromController(): Array<Array<Int>> {
        return Controller.controllerData.gridLayoutImgId
    }

    private fun getLatestPlayerPlayedMoveFromController(): ArrayList<Int> {
        return Controller.controllerData.playerPlayedMove
    }

    private fun getLatestAgentPlayedMoveFromController(): ArrayList<Int> {
        return Controller.controllerData.agentPlayedMove
    }

    private fun getLatestWinnerStateFromController(): ArrayList<Triple<Int, Int, Int>> {
        return Controller.controllerData.winnerState
    }

    private fun getLatestGameTypeFromController(): GameType {
        return Controller.controllerData.gameType
    }

    private fun getLatestCurrentTurnImgFromController(): Int {
        return Controller.controllerData.currentTurnImg
    }

    private fun getLatestCellBridgeFromController(): CellBridge {
        return CellBridge(content = Controller.controllerData.gameState.notVisitedCell.content)
    }


    private fun getLatestCurrentTurnFromController(): String {
        return Controller.controllerData.gameState.currentTurn().cellType.name
    }

    private fun getLatestListOfMovesFromController(): MutableList<Pair<Int, Int>> {
        return Controller.controllerData.gameState.listOfMoves
    }

    private fun getLatestPlayerBridgeListFromController(): MutableList<PlayerBridge> {
        return mutableListOf<PlayerBridge>().apply {
            add(PlayerBridge(getLatestListOfPlayersFromController()[0].cellType))
            add(PlayerBridge(getLatestListOfPlayersFromController()[1].cellType))
        }
    }

    private fun getLatestListOfPlayersFromController(): MutableList<Player> {
        return Controller.controllerData.gameState.listOfPlayers
    }

    private fun getLatestMatrixFromController(): Array<Array<Cell>> {
        return Controller.controllerData.gameState.grid.matrix
    }

    private fun getLatestGridBridgeFromController(): GridBridge {
        return GridBridge(
            dim = 3,
            matrix = kotlin.run {
                List(3) { row ->
                    List(3) { col ->
                        CellBridge(
                            row,
                            col,
                            getLatestMatrixFromController()[row][col].content
                        )
                    }
                }
            }
        )
    }

    /** ***********************************************************************************************************************/
    /** ********************************************** Updating Controller Data ***********************************************/
    /** ***********************************************************************************************************************/

    fun updateControllerGameState(gameState: GameState) {
        Controller.updateGameState(gameState)
    }

    /** ****************************************************************************************************************************/
    /** ********************************************** Updating Game Screen UI State ***********************************************/
    /** ****************************************************************************************************************************/

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

    fun updateShowSuggestMove(state: Boolean) {
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

    fun updateSuggestMoveCoordinates(row: Int, col: Int) {
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

    /** ****************************************************************************************************************************/
    /** ********************************************** UI Data Classes *************************************************************/
    /** ****************************************************************************************************************************/

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