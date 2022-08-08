package com.example.tictactoe.entry

import User
import android.os.Bundle
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
import com.example.tictactoe.gamescreen.GameScreenFragmentDirections
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.UserData

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
    }

    private fun setClickableButtons(){
        val settingsData = Controller.settings
        binding.apply {
            playerVsPlayer.setOnClickListener{v : View ->
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToGameScreenFragment(GameType.PLAYER_VS_PLAYER.name))
            }
            playerVsAi.setOnClickListener {v : View ->
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToGameScreenFragment(GameType.PLAYER_VS_AI.name))
            }
            settings.setOnClickListener {v : View ->
                v.findNavController().navigate(EntryFragmentDirections.actionEntryFragmentToSettingsFragment())
            }
        }
    }

    /** Temp checks */
    override fun onStart() {
        super.onStart()
        println("EntryFragment: onStart")
    }

    override fun onResume() {
        super.onResume()
        println("EntryFragment: onResume")
    }

    override fun onPause() {
        super.onPause()
        println("EntryFragment: onPause")
    }

    override fun onStop() {
        super.onStop()
        println("EntryFragment: onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("EntryFragment: OnDestroy")
    }
}