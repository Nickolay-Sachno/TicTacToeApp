package com.example.tictactoe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.tictactoe.databinding.FragmentHostGameScreenBinding
import com.example.tictactoe.gamescreen.GameScreenFragment
import com.example.tictactoe.movestracking.MovesTrackingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HostGameScreenFragment : Fragment() {
    lateinit var binding: FragmentHostGameScreenBinding
    lateinit var bottomNav: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_host_game_screen, container, false)

        loadFragment(GameScreenFragment())

        // set the bottom nav btn to load the fragment
        bottomNav = binding.bottomNav
        bottomNav.setOnItemSelectedListener { m : MenuItem ->
            when(m.itemId){
                R.id.board -> loadFragment(GameScreenFragment())
                R.id.moves -> loadFragment(MovesTrackingFragment())
                else -> {true}
            }
        }

        return binding.root
    }

    // load the fragment
    private fun loadFragment(fragment: Fragment): Boolean {
        println("Loading fragment: $fragment")
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
        return true
    }
}