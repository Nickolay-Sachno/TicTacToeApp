package com.example.tictactoe.gamescreen

import com.example.tictactoe.View


interface IGameScreenView : View {

    fun setCellImg(row:Int, col:Int, ingId: Int)
    fun navigateToStart(msg : String)
    fun restoreViewFromController()
    fun setFragmentClickable(name: String)
}