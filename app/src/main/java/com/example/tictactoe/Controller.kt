package com.example.tictactoe

import Cell
import GameState
import Grid
import Player
import User
import android.accounts.NetworkErrorException
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.example.tictactoe.entry.IWelcomeScreenView
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.gamescreen.IGameScreenView
import com.example.tictactoe.networking.RestClient
import com.example.tictactoe.networking.Result
import com.example.tictactoe.settings.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.reflect.typeOf


private const val FIRST_PLAYER_WIN = "First Player Wins!"
private const val SECOND_PLAYER_WINS = "Second Player Wins!"
private const val STANDOFF = "It's a Standoff"

private const val LOCK = "LOCK"
private const val UNLOCK = "UNLOCK"

private const val VISIBLE = "visible"
private const val INVISIBLE = "invisible"
private const val GONE = "gone"

// delays
private const val AGENT_DELAY_MOVE_TIME: Long = 1000
private const val SUGGESTED_MOVE_ANIM_DELAY_TIME: Long = 1500

object Controller : IController {

    var settings: Settings = Settings()
    var fragment: View? = null
    var winnerState: ArrayList<Triple<Int, Int, Int>> = arrayListOf()
    var playerPlayedMove: ArrayList<Int> = arrayListOf()
    var agentPlayedMove: ArrayList<Int> = arrayListOf()
    var currentTurnImg: Int = CellTypeImg.CROSS_BLACK.id

    var controllerData: ControllerData = ControllerData()

    override fun onCellSelected(row: Int, col: Int){
        val gameState : GameState = controllerData.gameState
        // if the cell already been visited
        if(gameState.getCell(row,col).content != CellType.EMPTY) return

        // if first player wins, ai shouldn't make a move
        if(gameState.isWinState(Cell(content = controllerData.firstPlayer.player.cellType)) || gameState.isStandOff()) {
            return
        }
    }

    fun checkIfPlayerWinAgent(): Boolean{
        val gameState : GameState = controllerData.gameState
        return gameState.isWinState(Cell(content = controllerData.firstPlayer.player.cellType)) || gameState.isStandOff()
    }

    override fun checkForWinner() : PlayerData?{
        val gameState : GameState = controllerData.gameState

        when{
            // first player wins
            gameState.isWinState(Cell(content = controllerData.firstPlayer.player.cellType)) -> {
                inflateWinner(controllerData.firstPlayer)
                return controllerData.firstPlayer}
            // second player wins
            gameState.isWinState(Cell(content = controllerData.secondPlayer.player.cellType)) -> {
                inflateWinner(controllerData.secondPlayer)
                return controllerData.secondPlayer
            }
            // standoff
            gameState.isStandOff() -> {
                return null
            }
        }
        // there are still moves to make
        return null
    }
    //TODO Move to View Model
    override fun setFragment(fragment: Any) {
        this.fragment = when(fragment){
            is IWelcomeScreenView -> fragment
            is IGameScreenView -> fragment
            is ISettingsView -> fragment
            else -> throw IllegalArgumentException()
        }
    }

    override fun setGameType(gameType: String) {
        updateGameType(when(gameType){
            GameType.PLAYER_VS_PLAYER.name -> GameType.PLAYER_VS_PLAYER
            GameType.PLAYER_VS_AI.name -> GameType.PLAYER_VS_AI
            else -> {
                // default game type
                GameType.PLAYER_VS_PLAYER
            }
        })
    }

    // updates the game state based on settings
    override fun createGameBasedOnTypeGame() {
        // create players based on game type and agent difficulty
        when(this.controllerData.gameType){
            GameType.PLAYER_VS_PLAYER -> {
                updateSecondPlayer(UserData(
                    player = User(cellType = CellType.CIRCLE),
                    cellTypeImg = CellTypeImg.CIRCLE_BLACK,
                    winCellTypeImg = CellTypeImg.CIRCLE_RED
                ))
            }
            GameType.PLAYER_VS_AI -> {
                updateSecondPlayer(AgentData(
                    player = Agent.createAgent(
                        cellType = CellType.CIRCLE,
                        diff = controllerData.settings.agentDifficulty
                    ) as Player
                ))
            }
        }
        updateGameState(GameState(
            grid = Grid(3),
            listOfPlayers = mutableListOf(this.controllerData.firstPlayer.player, this.controllerData.secondPlayer.player),
            notVisitedCell = Cell(),
            listOfMoves = mutableListOf()
        ))
    }

    private fun updateGridLayoutImgId(gridLayoutImgId: Array<IntArray>) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = controllerData.gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = controllerData.secondPlayer,
            listOfActions = controllerData.listOfActions,
            gameState = controllerData.gameState,
            gridLayoutImgId = gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = controllerData.currentTurnImg
        )
    }

    fun updateListOfActions(listOfActions: MutableList<GameState>) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = controllerData.gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = controllerData.secondPlayer,
            listOfActions = listOfActions,
            gameState = controllerData.gameState,
            gridLayoutImgId = controllerData.gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = controllerData.currentTurnImg
        )
    }

    private fun updateSecondPlayer(secondPlayer: PlayerData) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = controllerData.gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = secondPlayer,
            listOfActions = controllerData.listOfActions,
            gameState = controllerData.gameState,
            gridLayoutImgId = controllerData.gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = controllerData.currentTurnImg
        )
    }

    //TODO Move to View Model
    override fun inflateWinner(winPlayer: PlayerData) {

        winnerState.apply {
            add(
                Triple(
                    controllerData.gameState.listOfWinMoves[0].first,
                    controllerData.gameState.listOfWinMoves[0].second,
                    winPlayer.winCellTypeImg.id)
            )
            add(
                Triple(
                    controllerData.gameState.listOfWinMoves[1].first,
                    controllerData.gameState.listOfWinMoves[1].second,
                    winPlayer.winCellTypeImg.id)
            )
            add(
                Triple(
                    controllerData.gameState.listOfWinMoves[2].first,
                    controllerData.gameState.listOfWinMoves[2].second,
                    winPlayer.winCellTypeImg.id)
            )
        }
    }
    //TODO Move to View Model
    override fun lockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(LOCK)
        }
    }
    //TODO Move to View Model
    override fun unlockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(UNLOCK)
        }
    }
    //TODO Move to View Model
    override fun setFragmentProgressBarVisibility(view:View?, name: String) {
        when(view){
            is IGameScreenView -> view.setProgressBarVisibility(name)
        }
    }
    //TODO Move to View Model
    override fun getNextMoveHelpFromApi(context: Context) {

        val gameState : GameState = controllerData.gameState
        val gameScreenFragment = fragment as IGameScreenView

        gameScreenFragment.setFragmentClickable(LOCK)

        // Check for Internet connection
        if (!isOnline(context)){
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            // start the animation of the progress bar
            CoroutineScope(Main).launch{
                setFragmentProgressBarVisibility(gameScreenFragment, VISIBLE)
                delay(SUGGESTED_MOVE_ANIM_DELAY_TIME)
            }

            val game : String = getValidStringToApiCallFromGameState(gameState.toString())
            val turn = gameState.currentTurn().cellType.chr.toString()

            apiCallForNextMove(game, turn)

        } catch (e : Exception){
            Log.e("CONTROLLER", e.toString())
        } finally {
            setFragmentProgressBarVisibility(gameScreenFragment, INVISIBLE)
            gameScreenFragment.setFragmentClickable(UNLOCK)
        }
    }
    //TODO Move to View Model
    private fun apiCallForNextMove(game : String, turn: String) {
        try {
            val call: Call<Result> =
                RestClient.movesService.getNextMove(game, turn)
            call.enqueue(object : Callback<Result> {
                override fun onFailure(call: Call<Result>, t: Throwable) {
                    throw NetworkErrorException()
                }

                override fun onResponse(call: Call<Result>, response: Response<Result>) {
                    Log.i("ENTRY", "Finished with the response:\n${response.body()}")

                    val (row: Int, col: Int) = responseRecommendationToCoordinates(response.body()?.recommendation)

                    // start the animation of the progress bar
                    CoroutineScope(Main).launch{
                        setFragmentProgressBarVisibility(fragment, INVISIBLE)
                    }

                    animateSuggestedMoveOnBoard(row, col)
                }
            })
        } catch (e: Exception) {
            throw e
        }
    }
    //TODO Move to View Model
    private fun animateSuggestedMoveOnBoard(row: Int, col: Int){

        val gameScreenFragment = fragment as IGameScreenView

        CoroutineScope(Main).launch {
            gameScreenFragment.setCellBoardBackgroundColor(row,col, Color.GREEN)
            gameScreenFragment.setFragmentClickable(LOCK)

            delay(SUGGESTED_MOVE_ANIM_DELAY_TIME)

            gameScreenFragment.setFragmentClickable(UNLOCK)
            gameScreenFragment.setCellBoardBackgroundColor(row,col, Color.WHITE)
        }
    }

    private fun responseRecommendationToCoordinates(recommendation: Int?): Pair<Int, Int> {
        return when(recommendation){
            0 -> Pair(0,0) // (0,0).first -> 0
            1 -> Pair(0,1) // (0,1).second -> 1
            2 -> Pair(0,2)
            3 -> Pair(1,0)
            4 -> Pair(1,1)
            5 -> Pair(1,2)
            6 -> Pair(2,0)
            7 -> Pair(2,1)
            8 -> Pair(2,2)
            else -> Pair(0,0)
        }
    }

    override fun isOnline(context: Context): Boolean {
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

    override fun updateCurrentTurnImg(gameStateCurrentTurnImg: Int) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = controllerData.gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = controllerData.secondPlayer,
            listOfActions = controllerData.listOfActions,
            gameState = controllerData.gameState,
            gridLayoutImgId = controllerData.gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = gameStateCurrentTurnImg
        )
    }

    override fun updateGameState(gameState: GameState) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = controllerData.gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = controllerData.secondPlayer,
            listOfActions = controllerData.listOfActions,
            gameState = gameState,
            gridLayoutImgId = controllerData.gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = controllerData.currentTurnImg
        )
    }

    override fun updateGameType(gameType: GameType) {
        controllerData = ControllerData(
            settings = controllerData.settings,
            gameType = gameType,
            firstPlayer = controllerData.firstPlayer,
            secondPlayer = controllerData.secondPlayer,
            listOfActions = controllerData.listOfActions,
            gameState = controllerData.gameState,
            gridLayoutImgId = controllerData.gridLayoutImgId,
            winnerState = controllerData.winnerState,
            playerPlayedMove = controllerData.playerPlayedMove,
            agentPlayedMove = controllerData.agentPlayedMove,
            currentTurnImg = controllerData.currentTurnImg
        )
    }

    //TODO Move to View Model
    override fun playAgent() {

        var gameState : GameState = controllerData.gameState

        val currentPlayer = gameState.getNextPlayer()
        val imgId = CellTypeImg.CIRCLE_BLACK.id
        val (row, col) = currentPlayer.getNextMove(gameState)
        gameState.setCell(Cell(
            row = row,
            col = col,
            content = currentPlayer.cellType
        ))

        gameState.listOfMoves.add(Pair(row,col))
        gameState = gameState.updateGameState()

        // set turn img
        currentTurnImg = CellTypeImg.CIRCLE_BLACK.id
        // set grid cell img
        agentPlayedMove = arrayListOf(row, col, imgId)

        // set cell img in the grid layout
        controllerData.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        controllerData.listOfActions.add(gameState)

        checkForWinnerAndNavigateToStart()
    }
    //TODO Move to View Model
    override fun playUser(row: Int, col: Int) {
        // lock user from input on cells
        //fragment?.let { lockUserScreen(it) }

        var gameState : GameState = controllerData.gameState

        val imgId : Int = setUserImgId(row, col)

        // logic
        gameState.getNextPlayer()
        gameState.listOfMoves.add(Pair(row,col))
        gameState = gameState.updateGameState()

        playerPlayedMove = arrayListOf(row, col, imgId)

        // set cell img in the grid layout
        controllerData.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        controllerData.listOfActions.add(gameState)

        checkForWinnerAndNavigateToStart()

    }

    //TODO Move to View Model
    private fun checkForWinnerAndNavigateToStart(){
        if(fragment is IGameScreenView) {
            val gameScreenFragment = fragment as IGameScreenView
            val gameState : GameState = controllerData.gameState
            when (checkForWinner()) {
                controllerData.firstPlayer -> {
                    gameScreenFragment.navigateToStart(FIRST_PLAYER_WIN)
                }
                controllerData.secondPlayer -> {
                    gameScreenFragment.navigateToStart(SECOND_PLAYER_WINS)
                }
                else -> {
                    if (gameState.isStandOff()) {
                        gameScreenFragment.navigateToStart(STANDOFF)
                    }
                }
            }
        }
        else{
            throw IllegalArgumentException()
        }
    }
    //TODO Move to View Model
    private fun setUserImgId(row: Int, col: Int) : Int{
        val gameState : GameState = controllerData.gameState

        return when(gameState.currentTurn().cellType){
            CellType.CROSS -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CROSS
                ))
                currentTurnImg = CellTypeImg.CIRCLE_BLACK.id
                CellTypeImg.CROSS_BLACK.id
            }
            CellType.CIRCLE -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CIRCLE
                ))
                currentTurnImg = CellTypeImg.CROSS_BLACK.id
                CellTypeImg.CIRCLE_BLACK.id
            }
            else -> throw IllegalStateException()
        }
    }

     fun getValidStringToApiCallFromGameState(str: String) : String{
        var returnString = ""
        for( i in str.indices){
            if(str[i].toString() == "-" || str[i].toString() == "X" || str[i].toString() == "O"){
                returnString += str[i]
            }
        }
        return returnString
    }

    fun fromStringToGameStateBridge(str: String): GameStateBridge{
        return Gson().fromJson(str, GameStateBridge::class.java)
    }

    fun fromGameStateBridgeToString(gameStateBridge: GameStateBridge): String{
        return Gson().toJson(gameStateBridge)
    }
}

data class ControllerData(
    val settings: SettingsData = SettingsData(),
    val gameType: GameType = GameType.PLAYER_VS_AI,
    val firstPlayer : PlayerData = UserData(),
    val secondPlayer : PlayerData = AgentData(),
    val listOfActions : MutableList<GameState> = mutableListOf(),
    val gameState: GameState = GameState(
        grid = Grid(3),
        listOfPlayers = mutableListOf(firstPlayer.player, secondPlayer.player),
        notVisitedCell = Cell(),
        listOfMoves = mutableListOf()
    ),
    // var for recreating the Game Screen Fragment
    val gridLayoutImgId: Array<IntArray> = Array(3){IntArray(3)},
    val winnerState: ArrayList<Triple<Int, Int, Int>> = arrayListOf(),
    val playerPlayedMove: ArrayList<Int> = arrayListOf(),
    val agentPlayedMove: ArrayList<Int> = arrayListOf(),
    val currentTurnImg: Int = CellTypeImg.CROSS_BLACK.id
)