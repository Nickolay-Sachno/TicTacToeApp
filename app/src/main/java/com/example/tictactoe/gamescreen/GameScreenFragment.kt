package com.example.tictactoe.gamescreen

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.work.ListenableWorker
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.databinding.FragmentGameScreenBinding
import com.example.tictactoe.notification.BackToGameNotification
import com.example.tictactoe.repository.GameStateDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameScreenFragment : Fragment(), IGameScreenView {
    lateinit var binding: FragmentGameScreenBinding
    private val viewModel: GameScreenViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_screen, container, false)

        // Listener for Move Helper Button
        binding.nextMoveHelper.setOnClickListener {
            this.context?.let { context: Context -> nextMoveBtnClicked(context) }
        }

        // init the database
        viewModel.database = GameStateDatabase.getInstance(requireNotNull(this.activity).application)!!.gameStateDatabaseDao
        // init the repository
        viewModel.repository = GameStateDatabaseRepository(viewModel.database)
        // inform the controller about the current fragment
        Controller.setFragment(this)

        val uiStateObserver = createUIStateObserver()
        viewModel.gameScreenUIStateMutableData.observe(viewLifecycleOwner, uiStateObserver)
        viewModel.inflateGameScreenFragment()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
//        val sharedPref = context!!.getSharedPreferences("", AppCompatActivity.MODE_PRIVATE)
//        val defaultValue = "currentWorkerRetry"
//        val numbersOfRetries = sharedPref.getInt(defaultValue, AppCompatActivity.MODE_PRIVATE)
//        Log.i("GameScreenFragment", "number of retries from shared pref: $numbersOfRetries")
//        // the game is unfinished
//        if (numbersOfRetries < 3) {
//            // Pop-up the notification
//            val notification = BackToGameNotification(context!!)
//            notification.createNotification(1)
//            with(sharedPref.edit()) {
//                putInt("currentWorkerRetry", numbersOfRetries + 1)
//                apply()
//            }
//        } else {
//            // No active game in database
//            with(sharedPref.edit()){
//                putInt("currentWorkerRetry", 0)
//                apply()
//            }
//        }
    }

    fun onGridCellSelected(row: Int, col: Int) {
        viewModel.onCellClicked(row, col)
    }

    override fun setCellImg(row: Int, col: Int, imgId: Int) {
        when ("$row,$col") {
            "0,0" -> binding.imageView00.setImageResource(imgId)
            "0,1" -> binding.imageView01.setImageResource(imgId)
            "0,2" -> binding.imageView02.setImageResource(imgId)
            "1,0" -> binding.imageView10.setImageResource(imgId)
            "1,1" -> binding.imageView11.setImageResource(imgId)
            "1,2" -> binding.imageView12.setImageResource(imgId)
            "2,0" -> binding.imageView20.setImageResource(imgId)
            "2,1" -> binding.imageView21.setImageResource(imgId)
            "2,2" -> binding.imageView22.setImageResource(imgId)
        }
    }

    override fun navigateToStart(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        CoroutineScope(Main).launch {
            delay(2000)
            try {
                activity?.onBackPressed()
            } catch (e: Exception) {
                return@launch
            }
        }
    }

    override fun setFragmentClickable(name: String) {
        when (name) {
            "LOCK" -> {
                lockCells()
                lockNextMoveBtn()
            }
            "UNLOCK" -> {
                unlockCells()
                unlockNextMoveBtn()
            }
        }
    }

    override fun setProgressBarVisibility(name: String) {
        binding.progressBar.visibility = when (name) {
            "visible" -> View.VISIBLE
            "invisible" -> View.INVISIBLE
            "gone" -> View.GONE
            else -> throw IllegalArgumentException()
        }
    }

    override fun nextMoveBtnClicked(context: Context) {
        viewModel.onNextMoveBtnClicked(context)
    }

    override fun setCellBoardBackgroundColor(row: Int, col: Int, color: Int) {
        Log.i("GAME SCREEN FRAGMENT", "Set Cell Board Background Color: " +
                "row: $row, col: $col, color: $color")
        when ("$row,$col") {
            "0,0" -> binding.imageView00.setBackgroundColor(color)
            "0,1" -> binding.imageView01.setBackgroundColor(color)
            "0,2" -> binding.imageView02.setBackgroundColor(color)
            "1,0" -> binding.imageView10.setBackgroundColor(color)
            "1,1" -> binding.imageView11.setBackgroundColor(color)
            "1,2" -> binding.imageView12.setBackgroundColor(color)
            "2,0" -> binding.imageView20.setBackgroundColor(color)
            "2,1" -> binding.imageView21.setBackgroundColor(color)
            "2,2" -> binding.imageView22.setBackgroundColor(color)
        }
    }

    override fun createUIStateObserver(): Observer<in GameScreenViewModel.GameScreenUIState> {
        return Observer<GameScreenViewModel.GameScreenUIState> { newState ->
            binding.apply {
                currentPlayerImg.setImageResource(newState.currentTurnImg)
                progressBar.visibility = newState.progressBarVisibility
                when(newState.lockScreen){
                    true -> setFragmentClickable("LOCK")
                    else -> setFragmentClickable("UNLOCK")
                }
                // we update the board cell's only if they were played
                for(row in newState.board.boardImg.indices){
                    for(col in newState.board.boardImg[0].indices){
                        if(newState.board.boardImg[row][col] != 0){
                            setCellImg(row, col, newState.board.boardImg[row][col])
                        }
                    }
                }
                if(newState.suggestedMoveCoordinatesAndColor.isNotEmpty()){
                    setCellBoardBackgroundColor(
                        row = newState.suggestedMoveCoordinatesAndColor[0],
                        col = newState.suggestedMoveCoordinatesAndColor[1],
                        color = newState.suggestedMoveCoordinatesAndColor[2]
                    )
                }
            }
        }
    }

    private fun lockCells() {
        binding.apply {
            imageView00.isEnabled = false
            imageView01.isEnabled = false
            imageView02.isEnabled = false
            imageView10.isEnabled = false
            imageView11.isEnabled = false
            imageView12.isEnabled = false
            imageView20.isEnabled = false
            imageView21.isEnabled = false
            imageView22.isEnabled = false
        }
    }

    private fun unlockCells() {
        binding.apply {
            imageView00.isEnabled = true
            imageView01.isEnabled = true
            imageView02.isEnabled = true
            imageView10.isEnabled = true
            imageView11.isEnabled = true
            imageView12.isEnabled = true
            imageView20.isEnabled = true
            imageView21.isEnabled = true
            imageView22.isEnabled = true
        }
    }

    private fun lockNextMoveBtn() {
        binding.nextMoveHelper.isEnabled = false
    }

    private fun unlockNextMoveBtn() {
        binding.nextMoveHelper.isEnabled = true
    }
}
