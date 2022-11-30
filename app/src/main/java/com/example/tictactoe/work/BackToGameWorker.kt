package com.example.tictactoe.work

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tictactoe.database.GameStateDatabase
import com.example.tictactoe.notification.BackToGameNotification
import com.example.tictactoe.repository.GameStateDatabaseRepository

class BackToGameWorker(private val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    // Giving a unique name to the worker
    companion object {
        const val WORK_NAME = "BackToGameWorker"
    }

    // Coroutine worker
    override suspend fun doWork(): Result {
        Log.i("BackToGameWorker", " doWork()")
        return try {
            val sharedPref = appContext.getSharedPreferences("TicTacToeApplication", AppCompatActivity.MODE_PRIVATE)
            val defaultValue = "currentWorkerRetry"
            val numbersOfRetries = sharedPref.getInt(defaultValue, 0) // change to const values
            // the game is unfinished
            if (checkIfGameIsInProgress(appContext)) {
                Log.i("BackToGameWorker", "number of retries from shared pref: $numbersOfRetries")
                // steps:
                // check if the game is not running
//                if (isAppRunning()) {
//                    return Result.success()
//                }
                // check if the notification is not seen
                // check from shared pref if max number of notifications hasn't been reached
                if (numbersOfRetries < 3) {
                    // Pop-up the notification
                    val notification = BackToGameNotification(appContext)
                    notification.createNotification(1)
                    with(sharedPref.edit()) {
                        putInt("currentWorkerRetry", numbersOfRetries + 1)
                        apply()
                    }
                }
            }
            if (numbersOfRetries > 2) {
                // No active game in database
                with(sharedPref.edit()) {
                    putInt("currentWorkerRetry", 0)
                    apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // add clean job

    private suspend fun checkIfGameIsInProgress(appContext: Context): Boolean {
        val repository = GameStateDatabaseRepository(
            GameStateDatabase.getInstance(
                appContext
            )!!.gameStateDatabaseDao
        )
        return repository.getAllGameStates().isNotEmpty()
    }

    // NOTE: Change from process checking to UI checking
    private fun isAppRunning(): Boolean {
        val services =
            (appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
        return services.firstOrNull { it.processName.equals("com.example.tictactoe", true) } != null
    }
}