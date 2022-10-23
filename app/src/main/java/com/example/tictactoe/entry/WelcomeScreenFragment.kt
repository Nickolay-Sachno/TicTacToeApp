package com.example.tictactoe.entry

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.enum.GameType
import com.example.tictactoe.gamescreen.GameScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WelcomeScreenFragment : Fragment(), IWelcomeScreenView {

    private lateinit var binding:FragmentEntryBinding
    private val viewModel: GameScreenViewModel by activityViewModels()

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting up clickable buttons
        setClickableButtons()
        Controller.createGameBasedOnTypeGame()

        //TODO Move to the View Model
        CoroutineScope(Job()).launch{
            if(viewModel.database.getAllGameStates().isNotEmpty()){
                binding.resume.visibility = View.VISIBLE
            }

        }
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
                v.findNavController().navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
        }
    }
}