package com.srikar.lifeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_entries")
data class BudgetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String = "Food",
    val type: String = "expense",    // "expense" or "income"
    val date: String = "",
    val notes: String = ""
)
