package com.example.tictactoe.gamescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
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

    // Add RecyclerView member
    private val recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        println("GameScreenFragment: onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("GameScreenFragment: onCreateVIew")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_screen, container, false)

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
        Controller.onGridCellSelected(row, col)
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
            else -> binding.currentPlayerImg.setImageResource(imgId)
        }
    }

    override fun navigateToStart(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        CoroutineScope(Main).launch {
            delay(2000)
            try{
                activity?.onBackPressed()
                //findNavController().navigate(R.id.action_gameScreenFragment_to_entryFragment)
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
}