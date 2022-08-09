package com.example.tictactoe.movestracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictactoe.Controller
import com.example.tictactoe.R
import com.example.tictactoe.databinding.FragmentMovesTrackingBinding

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
        adapter = RecyclerAdapter(Controller.settings.listOfActions)
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }
}