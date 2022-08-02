package com.example.tictactoe.entry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentEntryBinding
import com.example.tictactoe.settings.Settings

class EntryFragment : Fragment() {

    private lateinit var binding:FragmentEntryBinding
    private lateinit var controller: Controller
    private lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickableButtons()
    }

    private fun setClickableButtons(){
        binding.apply {
            //TODO: Inform the controller
            playerVsPlayer.setOnClickListener{v : View ->
                v.findNavController().navigate(R.id.action_entryFragment_to_gameScreenFragment)
            }
            playerVsAi.setOnClickListener {v : View ->
                v.findNavController().navigate(R.id.action_entryFragment_to_gameScreenFragment)
            }
            settings.setOnClickListener {v : View ->
                v.findNavController().navigate(R.id.action_entryFragment_to_settingsFragment)
            }
        }
    }
}