package com.srikar.lifeflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srikar.lifeflow.data.entity.Habit
import com.srikar.lifeflow.data.entity.HabitLog

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY name ASC")
    fun getAllHabits(): LiveData<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Habit Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: Long, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getLogsForHabit(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): LiveData<List<HabitLog>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date AND completed = 1")
    fun getCompletedCountForDate(date: String): LiveData<Int>

    @Query("UPDATE habits SET currentStreak = :streak, bestStreak = :best WHERE id = :id")
    suspend fun updateStreak(id: Long, streak: Int, best: Int)
}
