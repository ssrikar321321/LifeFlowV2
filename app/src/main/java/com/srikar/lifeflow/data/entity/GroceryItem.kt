package com.srikar.lifeflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isBought: Boolean = false,
    val addedAt: String = ""
)
