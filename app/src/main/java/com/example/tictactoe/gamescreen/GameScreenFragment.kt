package com.example.tictactoe.gamescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_screen, container, false)
        Controller.setFragment(this)
        var args = GameScreenFragmentArgs.fromBundle(arguments!!)
        Controller.setGameType(args.gameType)
        Controller.createGameBasedOnTypeGame()

        //binding.gridLayout.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        binding.changeViewBtn.setOnClickListener{ v: View ->
            when(binding.changeViewBtn.text){
                "Moves" -> changeToMoves()
                "Board" -> changeToBoard()
            }
        }
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
                findNavController().navigate(R.id.action_gameScreenFragment_to_entryFragment)
            } catch(e : Exception ){
                return@launch
            }
        }
    }

    override fun changeToBoard() {
        println("Change to Board")
        binding.apply {
            recycleView.visibility = View.INVISIBLE
            gridLayout.visibility = View.VISIBLE
            changeViewBtn.text = "Moves"
            changeBtnImageView.setImageResource(R.drawable.ic_menu_history_image)
        }
    }

    override fun changeToMoves() {
        println("Change to Moves")
        binding.apply {
            recycleView.visibility = View.VISIBLE
            gridLayout.visibility = View.INVISIBLE
            changeViewBtn.text = "Board"
            changeBtnImageView.setImageResource(R.drawable.ic_dialog_dialer)
        }
    }

    /** Temp checks */
    override fun onStart() {
        super.onStart()
        println("GameScreenFragment: onStart")
    }

    override fun onResume() {
        super.onResume()
        println("GameScreenFragment: onResume")
    }

    override fun onPause() {
        super.onPause()
        println("GameScreenFragment: onPause")
    }

    override fun onStop() {
        super.onStop()
        println("GameScreenFragment: onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("GameScreenFragment: OnDestroy")
    }
}