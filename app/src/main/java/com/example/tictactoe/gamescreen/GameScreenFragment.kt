package com.example.tictactoe.gamescreen

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentGameScreenBinding
import com.example.tictactoe.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList


class GameScreenFragment : Fragment(), IGameScreenView {
    lateinit var binding: FragmentGameScreenBinding
    lateinit var settings: Settings
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
        // inform the controller about the current fragment
        Controller.setFragment(this)

        // Observation
        viewModel.apply {
            currentTurnImgLiveData().observe(viewLifecycleOwner, Observer(::updateCurrentTurnImg))
            gridLiveData().observe(viewLifecycleOwner, Observer(::updateGridImg))
            isProgressBarVisibleLiveData().observe(viewLifecycleOwner, Observer(::updateProgressBar))
            isGameScreenBlockedLiveData().observe(viewLifecycleOwner, Observer(::setFragmentClickable))
            boardImgLiveData().observe(viewLifecycleOwner, Observer(::updateBoardImg))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

    }

    private fun updateBoardImg(board: Array<IntArray>){
        for(row in board.indices){
            for( col in board[0].indices){
                setCellImg(row, col, board[row][col])
            }
        }
    }

    private fun updateProgressBar(isVisible: Boolean) {
        if(isVisible){
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun updateCurrentTurnImg(imgId: Int) {
        binding.currentPlayerImg.setImageResource(imgId)
    }

    private fun updateGridImg(point: Triple<Int, Int, Int>) {
        setCellImg(point.first, point.second, point.third)
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
        Controller.getNextMoveHelpFromApi(context)
    }

    override fun setCellBoardBackgroundColor(row: Int, col: Int, color: Int) {
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
