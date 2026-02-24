package com.srikar.lifeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val room: String = "Kitchen",
    val frequency: String = "daily",  // "daily", "weekly", "biweekly", "monthly"
    val isDone: Boolean = false,
    val lastDoneDate: String? = null
)
