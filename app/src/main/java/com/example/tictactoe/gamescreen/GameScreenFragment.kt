package com.example.tictactoe.gamescreen

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentGameScreenBinding
import com.example.tictactoe.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameScreenFragment : Fragment(), IGameScreenView {
    lateinit var binding: FragmentGameScreenBinding
    lateinit var settings : Settings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_screen, container, false)

        // Listener for Move Helper Button
        binding.nextMoveHelper.setOnClickListener{
            this.context?.let { context : Context -> nextMoveBtnClicked(context) }
        }

        // set the view based on the Controller Settings
        if(!Controller.settings.gameState.grid.isEmpty())
            restoreViewFromController()

        // inform the controller about the current fragment
        Controller.setFragment(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
    }

    fun onGridSelected(row: Int, col: Int){
        Controller.onCellSelected(row, col)
    }

    override fun setCellImg(row: Int, col: Int, imgId: Int) {
        when("$row,$col"){
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

    override fun navigateToStart(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        CoroutineScope(Main).launch {
            delay(2000)
            try{
                activity?.onBackPressed()
            } catch(e : Exception ){
                return@launch
            }
        }
    }

    override fun restoreViewFromController() {
        binding.apply {
            currentPlayerImg.setImageResource(
                when(Controller.settings.gameState.currentTurn()){
                    Controller.settings.firstPlayer.player -> Controller.settings.firstPlayer.cellTypeImg.id
                    Controller.settings.secondPlayer.player -> Controller.settings.secondPlayer.cellTypeImg.id
                    else -> 0
                })
            imageView00.setImageResource(Controller.settings.gridLayoutImgId[0][0])
            imageView01.setImageResource(Controller.settings.gridLayoutImgId[0][1])
            imageView02.setImageResource(Controller.settings.gridLayoutImgId[0][2])
            imageView10.setImageResource(Controller.settings.gridLayoutImgId[1][0])
            imageView11.setImageResource(Controller.settings.gridLayoutImgId[1][1])
            imageView12.setImageResource(Controller.settings.gridLayoutImgId[1][2])
            imageView20.setImageResource(Controller.settings.gridLayoutImgId[2][0])
            imageView21.setImageResource(Controller.settings.gridLayoutImgId[2][1])
            imageView22.setImageResource(Controller.settings.gridLayoutImgId[2][2])
        }
    }

    override fun setFragmentClickable(name: String){
        when(name){
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

    override fun setTurnImg(imgId: Int) {
        binding.currentPlayerImg.setImageResource(imgId)
    }

    override fun setProgressBarVisibility(name: String) {
        binding.progressBar.visibility = when(name){
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
        when("$row,$col"){
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

    private fun lockCells(){
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

    private fun unlockCells(){
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

    private fun lockNextMoveBtn(){
        binding.nextMoveHelper.isEnabled = false
    }

    private fun unlockNextMoveBtn(){
        binding.nextMoveHelper.isEnabled = true
    }
}