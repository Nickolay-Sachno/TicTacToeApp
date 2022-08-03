package com.example.tictactoe.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.databinding.DataBindingUtil
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentMovesTrackingBinding
import com.example.tictactoe.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    lateinit var settings : Settings
    lateinit var controller: Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable("settings").let { settingsId ->
            settings = settingsId as Settings
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        when(settings.difficulty.diff){
            AgentDifficulties.EASY.name -> { binding.switchDiff.isChecked = false}
            AgentDifficulties.MEDIUM.name -> {binding.switchDiff.isChecked = true}
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchDiff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings.difficulty = AgentDifficulties.MEDIUM
            } else {
                settings.difficulty = AgentDifficulties.EASY
            }
        }


    }

    /** Temp checks */
    override fun onStart() {
        super.onStart()
        println("SettingsFragment: onStart")
    }

    override fun onResume() {
        super.onResume()
        println("SettingsFragment: onResume")
    }

    override fun onPause() {
        super.onPause()
        println("SettingsFragment: onPause")
    }

    override fun onStop() {
        super.onStop()
        println("SettingsFragment: onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("SettingsFragment: OnDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        println("SettingsFragment: onDetach")
    }
}