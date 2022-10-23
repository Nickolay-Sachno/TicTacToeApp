package com.example.tictactoe.entry

import Cell
import GameState
import Grid
import Player
import User
import android.os.Bundle
import android.os.ParcelUuid.fromString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.gamescreen.GameScreenViewModel
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.GameStateBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.attribute.PosixFilePermissions.fromString
import java.util.UUID.fromString

class WelcomeScreenFragment : Fragment(), IWelcomeScreenView {

    private lateinit var binding:FragmentEntryBinding
    private val viewModel: GameScreenViewModel by activityViewModels()
    private lateinit var gameState: GameState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)

        viewModel.database = GameStateDatabase.getInstance(requireNotNull(this.activity).application)!!.gameStateDatabaseDao

        // update the current fragment in the controller
        Controller.setFragment(this)

        // init the view model
        //TODO Move to the View Model
        CoroutineScope(Job()).launch{
            if(viewModel.database.getAllGameStates().isNotEmpty()){
                Log.i("WELCOME SCREEN", "All game states:\n${viewModel.database.getAllGameStates()}")
                binding.resume.visibility = View.VISIBLE
                val gameStateBridgeString = viewModel.database.getLatestGameState().gameState
                Log.i("WELCOME SCREEN", "Game State Bridge String from DB:\n${gameStateBridgeString}")
                val gameStateBridge = Controller.fromStringToGameStateBridge(gameStateBridgeString)
                Log.i("WELCOME SCREEN", "Game State Bridge from DB:\n${gameStateBridge}")
                gameState = GameState(
                    grid = Grid(
                        dim = 3,
                        matrix = Array(3) { row ->
                            Array(3) { col ->
                                Cell(
                                    row = row,
                                    col = col,
                                    content = gameStateBridge.grid.matrix[row][col].content
                                )
                            }
                        }
                    ),
                    listOfPlayers = mutableListOf<Player>().apply {
                        add(User(gameStateBridge.listOfPlayers[0].cellType))
                        add(User(gameStateBridge.listOfPlayers[1].cellType))
                    },
                    listOfMoves = gameStateBridge.listOfMoves,
                    notVisitedCell = Cell(content = gameStateBridge.notVisitedCell.content)
                )
                Controller.currentTurnImg = when(viewModel.database.getLatestGameState().currentTurn){
                    CellType.CROSS.name -> CellTypeImg.CROSS_BLACK.id
                    else -> CellTypeImg.CIRCLE_BLACK.id
                }
                Controller.settings.gameState = gameState.deepCopy()
                Log.i("WELCOME SCREEN", "Last Game State from DB:\n$gameState")
            } else {
                Log.i("Game States in the Database: ", "No Game States found")
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting up clickable buttons
        setClickableButtons()
        Controller.createGameBasedOnTypeGame()


    }

    private fun setClickableButtons(){
        binding.apply {
            playerVsPlayer.setOnClickListener{v : View ->
                CoroutineScope(Job()).launch {
                    viewModel.database.clear()
                }
                activity?.viewModelStore?.clear() //TODO Move to Welcome Screen VM
                Controller.settings.typeGame = GameType.PLAYER_VS_PLAYER
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            playerVsAi.setOnClickListener {v : View ->
                CoroutineScope(Job()).launch {
                    viewModel.database.clear()
                }
                activity?.viewModelStore?.clear() //TODO Move to Welcome Screen VM
                Controller.settings.typeGame = GameType.PLAYER_VS_AI
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            settings.setOnClickListener {v : View ->
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToSettingsFragment())
            }
            resume.setOnClickListener{v: View ->
                Controller.settings.typeGame = GameType.PLAYER_VS_PLAYER
                Controller.createGameBasedOnTypeGame()
                for(row in 0..2){
                    for(col in 0..2){
                        viewModel.board[row][col] = when(gameState.getCell(row, col).content){
                            CellType.CROSS -> CellTypeImg.CROSS_BLACK.id
                            CellType.CIRCLE -> CellTypeImg.CIRCLE_BLACK.id
                            else -> {0}
                        }
                    }
                }
                viewModel.updateBoardImg()
                viewModel.setCurrentTurnImg(Controller.currentTurnImg)
                Controller.settings.gameState = gameState
                Log.i("WELCOME SCREEN", "Current Game State:${Controller.settings.gameState}")
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
        }
    }
}