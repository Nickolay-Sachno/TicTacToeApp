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
import com.example.tictactoe.gamescreen.IGameScreenView
import com.example.tictactoe.movestracking.MovesTrackingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.IllegalArgumentException

class HostGameScreenFragment : Fragment() {
    lateinit var binding: FragmentHostGameScreenBinding
    lateinit var bottomNav: BottomNavigationView
    lateinit var currentOpenFragment:Fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_host_game_screen, container, false)

        loadFragment(GameScreenFragment())

        // set the bottom nav btn to load the fragment
        bottomNav = binding.bottomNav
        currentOpenFragment = GameScreenFragment()
        bottomNav.setOnItemSelectedListener { m : MenuItem ->
            when(m.itemId){
                R.id.board -> {
                    loadFragmentIfNotLoaded(GameScreenFragment(), R.id.board)
                }
                R.id.moves -> {
                    loadFragmentIfNotLoaded(MovesTrackingFragment(), R.id.moves)
                }
                else -> {true}
            }
        }

        return binding.root
    }

    private fun loadFragmentIfNotLoaded(fragment: Fragment, id:Int) : Boolean{
        when(id){
            R.id.board ->{
                if(currentOpenFragment is MovesTrackingFragment) {
                currentOpenFragment = GameScreenFragment()
                loadFragment(GameScreenFragment())
                }
            }
            R.id.moves -> {
                if(currentOpenFragment is GameScreenFragment) {
                    currentOpenFragment = MovesTrackingFragment()
                    loadFragment(MovesTrackingFragment())
                }
            }
        }
        return true
    }

    // load the fragment
    private fun loadFragment(fragment: Fragment): Boolean {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        when(fragment) {
            is GameScreenFragment -> {
                transaction?.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            is MovesTrackingFragment -> {
                transaction?.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else -> throw IllegalArgumentException()
        }
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
        return true
    }
}