package com.example.tictactoe.movestracking

import GameState
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.R

class RecyclerAdapter(private var items: MutableList<GameState>) : RecyclerView.Adapter<RecyclerAdapter.RecycleViewHolder>() {

    override fun getItemCount() = items.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        return RecycleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_row, parent, false))
    }

    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        holder.apply {
            Log.i("Recycler Adapter", "items:\n${items[position].listOfMoves}")
            grid.text = items[position].grid.toString()
            move.text = items[position].listOfMoves[position].toString()
            player.text = "Player: ${items[position].listOfPlayers[1].cellType}"
        }
    }

    class RecycleViewHolder(v: View) : RecyclerView.ViewHolder(v){

        var grid: TextView
        var move: TextView
        var player: TextView


        init {
            grid = v.findViewById(R.id.current_grid)
            move = v.findViewById(R.id.last_move)
            player = v.findViewById(R.id.last_player_played)
        }
    }
}