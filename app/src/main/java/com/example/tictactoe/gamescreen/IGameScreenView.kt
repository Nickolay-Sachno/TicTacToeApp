package com.example.tictactoe.gamescreen

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.Observer
import com.example.tictactoe.View


interface IGameScreenView : View {

    fun setCellImg(row:Int, col:Int, imgId: Int)
    fun navigateToStart(text : String)
    fun setFragmentClickable(name: String)
    fun setProgressBarVisibility(name: String)
    fun nextMoveBtnClicked(context: Context)
    fun setCellBoardBackgroundColor(row: Int, col: Int, color: Int)
    fun createUIStateObserver(): Observer<in GameScreenViewModel.GameScreenUIState>
}