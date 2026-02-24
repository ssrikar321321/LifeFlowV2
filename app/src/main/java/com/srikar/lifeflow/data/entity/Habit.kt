package com.srikar.lifeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "✨",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val reminderTime: String = "08:00",  // HH:mm format
    val isActive: Boolean = true,
    val createdAt: String = ""
)
