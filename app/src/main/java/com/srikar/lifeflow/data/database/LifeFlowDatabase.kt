package com.srikar.lifeflow.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.srikar.lifeflow.data.dao.*
import com.srikar.lifeflow.data.entity.*

@Database(
    entities = [
        Task::class,
        Habit::class,
        HabitLog::class,
        Chore::class,
        GroceryItem::class,
        BudgetEntry::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LifeFlowDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun choreDao(): ChoreDao
    abstract fun groceryDao(): GroceryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: LifeFlowDatabase? = null

        fun getDatabase(context: Context): LifeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeFlowDatabase::class.java,
                    "lifeflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
