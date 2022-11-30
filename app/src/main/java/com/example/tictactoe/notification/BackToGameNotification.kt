package com.example.tictactoe.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.EXTRA_NOTIFICATION_CHANNEL_ID
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tictactoe.MainActivity
import com.example.tictactoe.R

class BackToGameNotification(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test_channel"
            val descriptionText = "my test channel"
            val importance = IMPORTANCE_DEFAULT

            val channel =
                NotificationChannel(EXTRA_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(notificationId: Int) {
        // creating the Intent for Main Activity to start the game
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // kills and start the app. NOTE: if the app is in background it should return to the game
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, EXTRA_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.tictactoe_navigation_img)
            .setContentTitle("Tic Tac Toe")
            .setContentText("Back To The Game")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notification is a unique int
            notify(notificationId, builder.build())
        }
    }

    // make fun clean to clean the context
}