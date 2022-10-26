package com.example.tictactoe.movestracking

import GameState
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentMovesTrackingBinding
import com.example.tictactoe.gamescreen.GameScreenViewModel

class MovesTrackingFragment : Fragment(), IMovesTrackingView {

    private lateinit var binding: FragmentMovesTrackingBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter : RecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_moves_tracking, container, false)

        // init the recycle view and the adapter
        linearLayoutManager = LinearLayoutManager(this.requireContext())
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        adapter = RecyclerAdapter(Controller.controllerData.listOfActions)
        binding.recyclerView.adapter = adapter

        return binding.root
    }
}