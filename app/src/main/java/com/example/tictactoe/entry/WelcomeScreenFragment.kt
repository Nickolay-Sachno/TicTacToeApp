package com.example.tictactoe.entry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.gamescreen.GameScreenViewModel

class WelcomeScreenFragment : Fragment(), IWelcomeScreenView {

    private lateinit var binding:FragmentEntryBinding
    private lateinit var viewModel: WelcomeScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)

        // update the current fragment in the controller
        Controller.setFragment(this)

        // init the view model
        viewModel = ViewModelProvider(this)[WelcomeScreenViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting up clickable buttons
        setClickableButtons()
        Controller.createGameBasedOnTypeGame()
    }

    private fun setClickableButtons(){
        activity?.viewModelStore?.clear() //TODO Move to Welcome Screen VM
        binding.apply {
            playerVsPlayer.setOnClickListener{v : View ->
                Controller.settings.typeGame = GameType.PLAYER_VS_PLAYER
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            playerVsAi.setOnClickListener {v : View ->
                Controller.settings.typeGame = GameType.PLAYER_VS_AI
                Controller.createGameBasedOnTypeGame()
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            settings.setOnClickListener {v : View ->
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToSettingsFragment())
            }
        }
    }
}