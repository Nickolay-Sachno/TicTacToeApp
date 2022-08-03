package com.example.tictactoe.gamescreen

import android.app.Activity
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentGameScreenBinding
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class GameScreenFragment : Fragment() {
    lateinit var binding: FragmentGameScreenBinding

    private lateinit var controller : Controller
    lateinit var settings : Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get the settings obj from the Bundle
        arguments?.getSerializable("settings").let { settingsId ->
            settings = settingsId as Settings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init the Controller
        controller = Controller(this, settings)
        // applying data bindings
        binding.controller = controller


    }

    fun setCellImg(row: Int, col: Int, imgId: Int) {
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

    fun navigateToEntryFragment(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        CoroutineScope(Main).launch {
            delay(2000)
            findNavController().navigate(R.id.action_gameScreenFragment_to_entryFragment)
        }
    }
}