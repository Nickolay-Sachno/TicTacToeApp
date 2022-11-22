package com.example.tictactoe.gamescreen

import Cell
import GameState
import Player
import android.accounts.NetworkErrorException
import android.content.Context
import android.graphics.Color
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
import com.example.tictactoe.networking.RestClient
import com.example.tictactoe.networking.Result
import com.example.tictactoe.repository.GameStateDatabaseRepository
import com.example.tictactoe.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// delays
private const val AGENT_DELAY_MOVE_TIME: Long = 1000
private const val SUGGESTED_MOVE_ANIM_DELAY_TIME: Long = 2000
private const val TAG: String = "GAME SCREEN VIEW MODEL"

class GameScreenViewModel() : ViewModel() {

    lateinit var database: GameStateDatabaseDao
    lateinit var repository: GameStateDatabaseRepository
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
        Log.i(TAG, "On Next Move Btn Clicked")
        // check for internet connection
        if (!hasInternetConnection(context)) {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            updateLockScreen(false)
            return
        }
        CoroutineScope(Main).launch {
            try {
                // lock the screen
                updateLockScreen(true)
                // start the animation of the progress bar with delay
                startProgressBarAnimation()

                Log.i(TAG, "Delay on Main Thread")
                delay(SUGGESTED_MOVE_ANIM_DELAY_TIME)
                // get current board as string
                val game: String =
                    getValidStringToApiCallFromGameState(getLatestGameStateFromController().toString())
                // get current turn as string
                val turn = getLatestCurrentTurnFromController().cellType.chr.toString()
                // send those to the API and get answer
                apiCallForSuggestMove(game, turn)

                clearSuggestMoveCoordinatesAndColor()

            } catch (e: Exception) {
                Log.i("Game Screen View Model", e.toString())
            } finally {
//                // end the animation of the progress bar
//                endProgressBarAnimation()
//                // unlock the screen
//                updateLockScreen(false)
            }
        }

    }

    private fun startProgressBarAnimation() {
        updateProgressBarVisibility(View.VISIBLE)
//        CoroutineScope(Main).launch {
//            Log.i(TAG, "Delay on Main Thread")
//            delay(SUGGESTED_MOVE_ANIM_DELAY_TIME)
//        }

    }

    private fun endProgressBarAnimation() {
        updateProgressBarVisibility(View.INVISIBLE)
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
                    repository.clear()
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
        playUserFromController(row, col)
        // update the board
        updateCellInBoardAfterPlayerPlayed()
        updateCurrentTurnImg(getLatestCurrentTurnImgFromController())
        updateLockScreen(false)
        // winner state
        if (getLatestWinnerStateFromController().size != 0) {
            updateWinnerUIState()
            clearDataFromControllerData()

            viewModelScope.launch {
                repository.clear()
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
        repository.insert(
            GameStateData(
                currentTurn = getLatestCurrentTurnCellTypeNameFromController(),
                gameState = Controller.fromGameStateBridgeToString(gameStateBridge)
            )
        )
    }

    fun getValidStringToApiCallFromGameState(str: String): String {
        var returnString = ""
        for (i in str.indices) {
            if (str[i].toString() == "-" || str[i].toString() == "X" || str[i].toString() == "O") {
                returnString += str[i]
            }
        }
        return returnString
    }

    /** ***********************************************************************************************************************/
    /** ********************************************** Suggest Move API Logic *************************************************/
    /** ***********************************************************************************************************************/

    private fun apiCallForSuggestMove(game: String, turn: String) {
        try {
            val call: Call<Result> =
                RestClient.movesService.getNextMove(game, turn)
            call.enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    throw NetworkErrorException()
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    Log.i("ENTRY", "Finished with the response:\n${response.body()}")

                    val (row: Int, col: Int) = formAPIRecommendationToCoordinates(
                        response.body()?.recommendation
                    )
                    // hide the progress bar after we get a response from API
                    updateProgressBarVisibility(View.INVISIBLE)

                    // animate the suggested move
                    animateSuggestedMoveOnBoard(row, col)

                    updateSuggestMoveCoordinatesAndColor(row, col, Color.WHITE)
                }
            })
        } catch (e: Exception) {
            throw e
        }
    }

    private fun animateSuggestedMoveOnBoard(row: Int, col: Int) {

        CoroutineScope(Main).launch {
            updateLockScreen(true)
            updateSuggestMoveCoordinatesAndColor(row, col, Color.GREEN)

            Log.i(TAG, "Delay on Main Thread")
            delay(SUGGESTED_MOVE_ANIM_DELAY_TIME)

            updateSuggestMoveCoordinatesAndColor(row, col, Color.WHITE)
            updateLockScreen(false)
        }
    }

    private fun formAPIRecommendationToCoordinates(recommendation: Int?): Pair<Int, Int> {
        return when (recommendation) {
            0 -> Pair(0, 0) // (0,0).first -> 0
            1 -> Pair(0, 1) // (0,1).second -> 1
            2 -> Pair(0, 2)
            3 -> Pair(1, 0)
            4 -> Pair(1, 1)
            5 -> Pair(1, 2)
            6 -> Pair(2, 0)
            7 -> Pair(2, 1)
            8 -> Pair(2, 2)
            else -> Pair(0, 0)
        }
    }

    private fun hasInternetConnection(context: Context): Boolean {
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

    /** ***********************************************************************************************************************/
    /** ********************************************** Get/Set data from/to Controller ****************************************/
    /** ***********************************************************************************************************************/

    private fun getLatestGameStateFromController(): GameState {
        return Controller.controllerData.gameState
    }

    private fun getLatestCurrentTurnFromController(): Player {
        return Controller.controllerData.gameState.currentTurn()
    }

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


    private fun getLatestCurrentTurnCellTypeNameFromController(): String {
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

    /** ***************************************************************************************************************************/
    /** ********************************************** Updating Controller Data ***************************************************/
    /** ***************************************************************************************************************************/

    fun updateControllerGameState(gameState: GameState) {
        Controller.updateGameState(gameState)
    }

    /** ****************************************************************************************************************************/
    /** ********************************************** Updating Game Screen UI State ***********************************************/
    /** ****************************************************************************************************************************/


    private fun clearSuggestMoveCoordinatesAndColor(){
        Log.i(TAG, "Clearing Suggest Move Coordinates and color ")
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen,
            suggestedMoveCoordinatesAndColor = listOf()
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateSuggestMoveCoordinatesAndColor(row: Int, col: Int, color: Int){
        Log.i(TAG, "Updating Suggest Move Coordinates and color with: " +
                "row: $row, col: $col, color: $color")
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen,
            suggestedMoveCoordinatesAndColor = listOf(row, col, color)
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateCurrentTurnImg(currentTurnImg: Int) {
        Log.i(TAG, "Updating Current Turn Img with: $currentTurnImg")
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = currentTurnImg,
            board = Board(
                boardImg = gameScreenUIState.board.boardImg
            ),
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen,
            suggestedMoveCoordinatesAndColor = gameScreenUIState.suggestedMoveCoordinatesAndColor
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateCellInBoard(row: Int, col: Int, imgId: Int) {
        Log.i(TAG, "updating Cell in Board with: " +
                "row: $row, col: $col, imgId: $imgId")
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
        Log.i(TAG, "Updating board with: \n$board")
        gameScreenUIState = GameScreenUIState(
            currentTurnImg = gameScreenUIState.currentTurnImg,
            board = board,
            progressBarVisibility = gameScreenUIState.progressBarVisibility,
            lockScreen = gameScreenUIState.lockScreen
        )
        gameScreenUIStateMutableData.postValue(gameScreenUIState)
    }

    fun updateProgressBarVisibility(visibility: Int) {
        Log.i(TAG, "Updating Progress Bar Visibility with: $visibility")
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
        Log.i(TAG, "Updating Lock Screen with: $lockScreen")
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
        Log.i(TAG, "Updating Show Suggest Move with: $state")
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
        Log.i(TAG, "Updating Suggest Move Coordinates with: row: $row, col: $col")
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
        val suggestMoveCoordinates: List<Int> = listOf(),
        val suggestedMoveCoordinatesAndColor: List<Int> = listOf()
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

        override fun toString(): String {
            var str: String = ""
            this.boardImg.forEach { row ->
                row.forEach { cell ->
                    str += cell.toString()
                }
                str += "\n"
            }
            return super.toString()
        }
    }

}