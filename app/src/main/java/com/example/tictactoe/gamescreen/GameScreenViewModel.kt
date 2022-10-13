package com.example.tictactoe.gamescreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tictactoe.Controller

class GameScreenViewModel : ViewModel() {

    private val currentTurnImgMutableLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val gridMutableLiveData: MutableLiveData<Triple<Int, Int, Int>> by lazy {
        MutableLiveData<Triple<Int, Int, Int>>()
    }

    private val isProgressBarVisibleMutableLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val fragmentLockStatusMutableLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun currentTurnImgLiveData() : LiveData<Int> = currentTurnImgMutableLiveData
    fun gridLiveData() : LiveData<Triple<Int, Int, Int>> = gridMutableLiveData
    fun isProgressBarVisibleLiveData() : LiveData<Boolean> = isProgressBarVisibleMutableLiveData
    fun isGameScreenBlockedLiveData() : LiveData<String> = fragmentLockStatusMutableLiveData


    fun setCurrentTurnImg(imgId: Int){
        currentTurnImgMutableLiveData.postValue(imgId)
    }

    fun onCellClicked(row: Int, col: Int) {
        Controller.onCellSelected(row, col)
    }

    init {
        Controller.gameScreenViewModel = this
    }

}