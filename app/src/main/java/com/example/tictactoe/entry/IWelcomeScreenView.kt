package com.example.tictactoe.entry

import androidx.lifecycle.Observer
import com.example.tictactoe.View


interface IWelcomeScreenView : View {
    fun initUiObserver(): Observer<in WelcomeScreenUiState>

}