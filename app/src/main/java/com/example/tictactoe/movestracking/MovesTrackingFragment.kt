package com.example.tictactoe.movestracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentMovesTrackingBinding

class MovesTrackingFragment : Fragment(), IMovesTrackingView {

    private lateinit var binding: FragmentMovesTrackingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_moves_tracking, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener{ v: View ->
            v.findNavController().navigate(R.id.action_movesTrackingFragment_to_gameScreenFragment)
        }
    }
}