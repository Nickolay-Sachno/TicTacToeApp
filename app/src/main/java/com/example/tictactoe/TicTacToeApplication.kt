package com.example.tictactoe

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tictactoe.work.BackToGameWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/*
* Override application to setup background work via WorkManager
* */
class TicTacToeApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)
    private var currentWorkerRetry: Int = -1

    override fun onCreate() {
        super.onCreate()
        Log.i("TicTacToeApplication", "onCreate()")
        runWorker()
        // wrap shared pref with external class
        if (currentWorkerRetry == -1) {
            currentWorkerRetry = 0
            val sharedPref = this.getSharedPreferences("TicTacToeApplication", Context.MODE_PRIVATE) ?: return
            // sharedPref.contains()
            with(sharedPref.edit()) {
                putInt("currentWorkerRetry", currentWorkerRetry)
                apply()
            }
        }
    }

    private fun runWorker() {
        Log.i("TicTacToeApplication", "runWorker()")
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        Log.i("TicTacToeApplication", "setupRecurringWork")
        val repeatingRequest = PeriodicWorkRequestBuilder<BackToGameWorker>(
            15,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            BackToGameWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}