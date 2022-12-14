package com.example.tictactoe.entry

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.repository.GameStateDatabaseRepository

class WelcomeScreenFragment : Fragment(), IWelcomeScreenView {

    private lateinit var binding: FragmentEntryBinding
    private val model: WelcomeScreenViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)
        // init the database
        model.database =
            GameStateDatabase.getInstance(requireNotNull(this.activity).application)!!.gameStateDatabaseDao
        // init repository
        model.repository = GameStateDatabaseRepository(model.database)
        // inflate the Fragment from View Model
        model.inflateWelcomeScreenFragment()
        // init Observer
        val uiStateObserver = initUiObserver()
        model.welcomeScreenUiStateMutableLiveData.observe(viewLifecycleOwner, uiStateObserver)

        // update the current fragment in the controller
        Controller.setFragment(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setting up clickable buttons
        setClickableButtons()
    }

    private fun setClickableButtons() {
        binding.apply {
            playerVsPlayer.setOnClickListener { v: View ->
                model.playerVsPlayerClicked()
                v.findNavController()
                    .navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            playerVsAi.setOnClickListener { v: View ->
                model.playerVsAiClicked()
                v.findNavController()
                    .navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
            settings.setOnClickListener { v: View ->
                v.findNavController()
                    .navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToSettingsFragment())
            }
            restoreGame.setOnClickListener { v: View ->
                model.restoreGameClicked()
                v.findNavController()
                    .navigate(WelcomeScreenFragmentDirections.actionEntryFragmentToHostGameScreenFragment())
            }
        }
    }

    override fun initUiObserver(): Observer<in WelcomeScreenUiState> {
        return Observer<WelcomeScreenUiState> { newState ->
            binding.apply {
                playerVsPlayer.visibility = newState.playerVsPlayerVisibility
                playerVsAi.visibility = newState.playerVsAiVisibility
                restoreGame.visibility = newState.restoreGameVisibility
                settings.visibility = newState.settingsVisibility
            }
        }
    }
}