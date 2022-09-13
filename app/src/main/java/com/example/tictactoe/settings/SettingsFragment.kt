package com.example.tictactoe.settings

import AgentDifficulties
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentSettingsBinding
import com.example.tictactoe.movestracking.MovesTrackingViewModel


class SettingsFragment : Fragment(), ISettingsView {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Controller.setFragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        // update the current fragment in the controller
        Controller.setFragment(this)

        when(Controller.settings.difficulty.name){
            AgentDifficulties.EASY.name -> { binding.switchDiff.isChecked = false}
            AgentDifficulties.MEDIUM.name -> {binding.switchDiff.isChecked = true}
        }

        // init the view model
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchDiff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Controller.settings.difficulty = AgentDifficulties.MEDIUM
            } else {
                Controller.settings.difficulty = AgentDifficulties.EASY
            }
        }


    }

    override fun getDiff(): AgentDifficulties {
        return Controller.settings.difficulty
    }

    override fun setDiff(diff: AgentDifficulties) {
        Controller.settings.difficulty = diff
    }
}