package com.example.tictactoe.movestracking

import GameState
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.Controller

class MovesTrackingViewModel : ViewModel() {

    val recyclerViewMutableLiveData: MutableLiveData<RecyclerView> by lazy {
        MutableLiveData<RecyclerView>()
    }

    fun addItemDecoration(context: Context, orientation: Int) {
        DividerItemDecoration(context, orientation)
    }

    fun inflateMovesTracking(context: Context) {
        val recyclerView = RecyclerView(context)
        recyclerView.adapter = RecyclerAdapter(getListOfActionsFromController())
        recyclerViewMutableLiveData.postValue(
            recyclerView
        )
    }

    private fun getListOfActionsFromController(): MutableList<GameState> {
        return Controller.controllerData.listOfActions
    }
}