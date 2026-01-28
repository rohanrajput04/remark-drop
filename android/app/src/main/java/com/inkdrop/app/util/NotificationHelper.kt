package com.inkdrop.app.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.inkdrop.app.InkDropApplication
import com.inkdrop.app.MainActivity
import com.inkdrop.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notificationId = 1

    fun showSuccess(title: String) {
        showNotification(
            title = "Sent to Kindle!",
            message = title,
            isError = false
        )
    }

    fun showError(message: String) {
        showNotification(
            title = "Failed to Send",
            message = message,
            isError = true
        )
    }

    fun showProgress(url: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, InkDropApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sending to Kindle...")
            .setContentText(url)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        notificationManager.notify(PROGRESS_NOTIFICATION_ID, notification)
    }

    fun dismissProgress() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    private fun showNotification(title: String, message: String, isError: Boolean) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, InkDropApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isError) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId++, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        private const val PROGRESS_NOTIFICATION_ID = 999
    }
}
