package com.srikar.lifeflow.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.srikar.lifeflow.LifeFlowApp
import com.srikar.lifeflow.R
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "LifeFlow Reminder"
        val message = inputData.getString("message") ?: "You have pending tasks!"
        val channelId = inputData.getString("channel") ?: LifeFlowApp.CHANNEL_TASKS

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }

    companion object {
        fun scheduleDaily(context: Context) {
            // Schedule morning reminder
            val morningData = Data.Builder()
                .putString("title", "Good Morning! 🌅")
                .putString("message", "Check your tasks and habits for today")
                .putString("channel", LifeFlowApp.CHANNEL_HABITS)
                .build()

            val morningRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInputData(morningData)
                .setInitialDelay(calculateDelay(8, 0), TimeUnit.MILLISECONDS)
                .addTag("morning_reminder")
                .build()

            // Schedule evening reminder
            val eveningData = Data.Builder()
                .putString("title", "Evening Check-in 🌙")
                .putString("message", "Did you complete all habits today?")
                .putString("channel", LifeFlowApp.CHANNEL_HABITS)
                .build()

            val eveningRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInputData(eveningData)
                .setInitialDelay(calculateDelay(21, 0), TimeUnit.MILLISECONDS)
                .addTag("evening_reminder")
                .build()

            val wm = WorkManager.getInstance(context)
            wm.enqueueUniquePeriodicWork("morning", ExistingPeriodicWorkPolicy.KEEP, morningRequest)
            wm.enqueueUniquePeriodicWork("evening", ExistingPeriodicWorkPolicy.KEEP, eveningRequest)
        }

        private fun calculateDelay(targetHour: Int, targetMinute: Int): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, targetHour)
                set(java.util.Calendar.MINUTE, targetMinute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderWorker.scheduleDaily(context)
        }
    }
}
