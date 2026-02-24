package com.srikar.lifeflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.srikar.lifeflow.data.database.LifeFlowDatabase

class LifeFlowApp : Application() {

    val database: LifeFlowDatabase by lazy {
        LifeFlowDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val habitChannel = NotificationChannel(
                CHANNEL_HABITS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit reminder notifications"
            }

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming task deadline notifications"
            }

            val choreChannel = NotificationChannel(
                CHANNEL_CHORES,
                "Chore Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Household chore reminders"
            }

            manager.createNotificationChannel(habitChannel)
            manager.createNotificationChannel(taskChannel)
            manager.createNotificationChannel(choreChannel)
        }
    }

    companion object {
        const val CHANNEL_HABITS = "habits_channel"
        const val CHANNEL_TASKS = "tasks_channel"
        const val CHANNEL_CHORES = "chores_channel"
    }
}
