package com.example.tictactoe.settings

import AgentDifficulties
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentSettingsBinding
import com.example.tictactoe.gamescreen.GameScreenViewModel


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val model: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        // update the current fragment in the controller

        val uiStateObserver = createUIStateObserver()
        model.settingsUIStateMutableData.observe(viewLifecycleOwner, uiStateObserver)

        //model.inflateUIState()

        return binding.root
    }

    private fun createUIStateObserver(): Observer<in SettingsUiState> {
        return Observer<SettingsUiState>{ newState ->
            when(newState.settingsData.agentDifficulty){
                AgentDifficulties.MEDIUM -> binding.switchDiff.isChecked = true
                AgentDifficulties.EASY -> binding.switchDiff.isChecked = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchDiff.setOnCheckedChangeListener { _, isChecked ->
            model.onClickSetAgentDifficulty(isChecked)
        }


    }
}