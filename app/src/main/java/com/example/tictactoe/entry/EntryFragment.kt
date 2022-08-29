package com.example.tictactoe.entry

import User
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.enum.GameType
import okhttp3.*
import java.io.IOException

class EntryFragment : Fragment(), IEntryView {

    private lateinit var binding:FragmentEntryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("EntryFragment: onCreateView")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)
        // update the current fragment in the controller
        Controller.setFragment(this)
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
                tempCall()
                Controller.settings.typeGame = GameType.PLAYER_VS_PLAYER
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            playerVsAi.setOnClickListener {v : View ->
                Controller.settings.typeGame = GameType.PLAYER_VS_AI
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            settings.setOnClickListener {v : View ->
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToSettingsFragment())
            }
        }
    }

    private fun tempCall(){
        try{
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://stujo-tic-tac-toe-stujo-v1.p.rapidapi.com/X-O-XO---/X")
                .get()
                .addHeader("X-RapidAPI-Key", "c26ff06453msh9930f7571f03f91p154383jsn028a87338af9")
                .addHeader("X-RapidAPI-Host", "stujo-tic-tac-toe-stujo-v1.p.rapidapi.com")
                .build()

            client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("ENTRY", "Could not connect ot the server")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.i("ENTRY", "Finished with the response:\n${response}")
                    }
                }
            )
        } finally {

        }
    }
}