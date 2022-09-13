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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException


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

    override fun onCellSelected(row: Int, col: Int){
        val gameState : GameState = settings.gameState
        // if the cell already been visited
        if(gameState.getCell(row,col).content != CellType.EMPTY) return

        when(settings.typeGame){
            GameType.PLAYER_VS_PLAYER -> playUser(row,col)
            GameType.PLAYER_VS_AI -> {
                playUser(row,col)
                // if first player wins, ai shouldn't make a move
                if(gameState.isWinState(Cell(content = settings.firstPlayer.player.cellType)) || gameState.isStandOff()) {
                    return
                }
                playAgentWithDelay(AGENT_DELAY_MOVE_TIME)
            }
        }
    }

    override fun checkForWinner() : PlayerData?{
        val gameState : GameState = settings.gameState

        when{
            // first player wins
            gameState.isWinState(Cell(content = settings.firstPlayer.player.cellType)) -> {
                inflateWinner(settings.firstPlayer)
                return settings.firstPlayer}
            // second player wins
            gameState.isWinState(Cell(content = settings.secondPlayer.player.cellType)) -> {
                inflateWinner(settings.secondPlayer)
                return settings.secondPlayer
            }
            // standoff
            gameState.isStandOff() -> {
                return null
            }
        }
        // there are still moves to make
        return null
    }

    override fun setFragment(fragment: Any) {
        this.fragment = when(fragment){
            is IWelcomeScreenView -> fragment
            is IGameScreenView -> fragment
            is ISettingsView -> fragment
            else -> throw IllegalArgumentException()
        }
    }

    override fun setGameType(gameType: String) {
        settings.typeGame = when(gameType){
            GameType.PLAYER_VS_PLAYER.name -> GameType.PLAYER_VS_PLAYER
            GameType.PLAYER_VS_AI.name -> GameType.PLAYER_VS_AI
            else -> {
                // default game type
                GameType.PLAYER_VS_PLAYER
            }
        }
    }

    // updates the game state based on settings
    override fun createGameBasedOnTypeGame() {
        // create players based on game type and agent difficulty
        when(this.settings.typeGame){
            GameType.PLAYER_VS_PLAYER -> {
                settings.secondPlayer = UserData(
                    player = User(cellType = CellType.CIRCLE),
                    cellTypeImg = CellTypeImg.CIRCLE_BLACK,
                    winCellTypeImg = CellTypeImg.CIRCLE_RED
                )
            }
            GameType.PLAYER_VS_AI -> {
                settings.secondPlayer.player = Agent.createAgent(
                    cellType = CellType.CIRCLE,
                    diff = settings.difficulty
                ) as Player
            }
        }
        settings.gameState = GameState(
            grid = Grid(3),
            listOfPlayers = mutableListOf(this.settings.firstPlayer.player, this.settings.secondPlayer.player),
            notVisitedCell = Cell(),
            listOfMoves = mutableListOf()
        )

        settings.listOfActions = mutableListOf()

        // clear the Img grid
        settings.gridLayoutImgId = Array(3){IntArray(3)}
    }

    override fun inflateWinner(winPlayer: PlayerData) {
        if(fragment is IGameScreenView){
            val gameScreenFragment = fragment as IGameScreenView
            gameScreenFragment.apply {
                setCellImg(
                    settings.gameState.listOfWinMoves[0].first,
                    settings.gameState.listOfWinMoves[0].second,
                    winPlayer.winCellTypeImg.id)
                setCellImg(
                    settings.gameState.listOfWinMoves[1].first,
                    settings.gameState.listOfWinMoves[1].second,
                    winPlayer.winCellTypeImg.id)
                setCellImg(
                    settings.gameState.listOfWinMoves[2].first,
                    settings.gameState.listOfWinMoves[2].second,
                    winPlayer.winCellTypeImg.id)
            }
        } else throw IllegalArgumentException()
    }

    override fun lockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(LOCK)
        }
    }

    override fun unlockUserScreen(view: View) {
        when(view){
            is IGameScreenView -> view.setFragmentClickable(UNLOCK)
        }
    }

    override fun setFragmentProgressBarVisibility(view:View?, name: String) {
        when(view){
            is IGameScreenView -> view.setProgressBarVisibility(name)
        }
    }

    override fun getNextMoveHelpFromApi(context: Context) {

        val gameState : GameState = settings.gameState
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

    override fun playAgent() {

        var gameState : GameState = settings.gameState
        val gameScreenFragment = fragment as IGameScreenView

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
        gameScreenFragment.setTurnImg(CellTypeImg.CROSS_BLACK.id)
        // set grid cell img
        gameScreenFragment.setCellImg(row, col, imgId)

        // set cell img in the grid layout
        this.settings.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        settings.listOfActions.add(gameState)

        checkForWinnerAndNavigateToStart()
    }

    override fun playUser(row: Int, col: Int) {
        // lock user from input on cells
        fragment?.let { lockUserScreen(it) }

        var gameState : GameState = settings.gameState
        val gameScreenFragment = fragment as IGameScreenView

        val imgId : Int = setUserImgId(row, col)

        // logic
        gameState.getNextPlayer()
        gameState.listOfMoves.add(Pair(row,col))
        gameState = gameState.updateGameState()

        gameScreenFragment.setCellImg(row, col, imgId)

        // set cell img in the grid layout
        this.settings.gridLayoutImgId[row][col] = imgId

        // add current Game State to the history list
        settings.listOfActions.add(gameState)

        // unlock input for user
        fragment?.let { unlockUserScreen(it) }

        checkForWinnerAndNavigateToStart()

    }


    private fun checkForWinnerAndNavigateToStart(){
        if(fragment is IGameScreenView) {
            val gameScreenFragment = fragment as IGameScreenView
            val gameState : GameState = settings.gameState
            when (checkForWinner()) {
                settings.firstPlayer -> {
                    gameScreenFragment.navigateToStart(FIRST_PLAYER_WIN)
                }
                settings.secondPlayer -> {
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

    private fun setUserImgId(row: Int, col: Int) : Int{
        val gameState : GameState = settings.gameState
        val gameScreenFragment = fragment as IGameScreenView

        return when(gameState.currentTurn().cellType){
            CellType.CROSS -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CROSS
                ))
                gameScreenFragment.setTurnImg(CellTypeImg.CIRCLE_BLACK.id)
                CellTypeImg.CROSS_BLACK.id
            }
            CellType.CIRCLE -> {
                gameState.setCell(Cell(
                    row = row,
                    col = col,
                    content = CellType.CIRCLE
                ))
                gameScreenFragment.setTurnImg(CellTypeImg.CROSS_BLACK.id)
                CellTypeImg.CIRCLE_BLACK.id
            }
            else -> throw IllegalStateException()
        }
    }

    override fun playAgentWithDelay(timeMill: Long){
        CoroutineScope(Main).launch {

            fragment?.let { it ->
                // lock user from input on cells
                lockUserScreen(it)
                // display progress bar on the screen
                setFragmentProgressBarVisibility(it, VISIBLE)
            }

            // wait second
            delay(timeMill)

            // agent make a move
            playAgent()

            fragment?.let { it ->
                // hide progressbar on the screen
                setFragmentProgressBarVisibility(it, INVISIBLE)
                // unlock input for user
                unlockUserScreen(it)
            }
        }
    }

    private fun getValidStringToApiCallFromGameState(str: String) : String{
        var returnString = ""
        for( i in str.indices){
            if(str[i].toString() == "-" || str[i].toString() == "X" || str[i].toString() == "O"){
                returnString += str[i]
            }
        }
        return returnString
    }
}