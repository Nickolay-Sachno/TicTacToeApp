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
import com.example.tictactoe.settings.CellTypeImg
import com.example.tictactoe.settings.Settings
import com.example.tictactoe.settings.UserData

class EntryFragment : Fragment() {

    private lateinit var binding:FragmentEntryBinding
    lateinit var settingsData: Settings
    lateinit var controller: Controller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("EntryFragment: onCreateView")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init default settings
        settingsData = Settings()


        // setting up clickable buttons
        setClickableButtons()
    }

    private fun setClickableButtons(){
        val bundle = Bundle()
        binding.apply {
            //TODO: Inform the controller
            playerVsPlayer.setOnClickListener{v : View ->
                // change the type game
                settingsData.apply {
                    typeGame = "playerVsPlayer"
                    secondPlayer = UserData(
                        player = User(cellType = CellType.CIRCLE),
                        cellTypeImg = CellTypeImg.CIRCLE_BLACK,
                        winCellTypeImg = CellTypeImg.CIRCLE_RED
                    )
                    gameState.listOfPlayers[1] = secondPlayer.player
                }
                bundle.putSerializable("settings", settingsData)
                v.findNavController().navigate(R.id.action_entryFragment_to_gameScreenFragment, bundle)
            }
            playerVsAi.setOnClickListener {v : View ->
                bundle.putSerializable("settings", settingsData)
                v.findNavController().navigate(R.id.action_entryFragment_to_gameScreenFragment, bundle)
            }
            settings.setOnClickListener {v : View ->
                bundle.putSerializable("settings", settingsData)
                v.findNavController().navigate(R.id.action_entryFragment_to_settingsFragment, bundle)
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