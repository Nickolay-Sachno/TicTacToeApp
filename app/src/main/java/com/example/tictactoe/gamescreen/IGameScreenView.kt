package com.example.tictactoe.gamescreen

import android.content.Context
import android.graphics.Color
import com.example.tictactoe.View


interface IGameScreenView : View {

    fun setCellImg(row:Int, col:Int, imgId: Int)
    fun navigateToStart(msg : String)
    fun restoreViewFromController()
    fun setFragmentClickable(name: String)
    fun setTurnImg(imgId: Int)
    fun setProgressBarVisibility(name: String)
    fun nextMoveBtnClicked(context: Context)
    fun setCellBoardBackgroundColor(row: Int, col: Int, color: Int)
}