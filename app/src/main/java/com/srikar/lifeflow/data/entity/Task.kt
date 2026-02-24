package com.srikar.lifeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "UKAEA Prep",
    val priority: String = "medium",   // "high", "medium", "low"
    val deadline: String? = null,       // ISO date string "2026-04-15"
    val isDone: Boolean = false,
    val postponedCount: Int = 0,
    val createdAt: String = "",
    val notes: String = ""
)
