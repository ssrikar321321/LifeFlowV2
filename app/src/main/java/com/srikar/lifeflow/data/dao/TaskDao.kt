package com.srikar.lifeflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srikar.lifeflow.data.entity.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isDone ASC, CASE priority WHEN 'high' THEN 0 WHEN 'medium' THEN 1 ELSE 2 END, deadline ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY CASE priority WHEN 'high' THEN 0 WHEN 'medium' THEN 1 ELSE 2 END, deadline ASC")
    fun getActiveTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDone = 1")
    fun getCompletedTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isDone ASC, deadline ASC")
    fun getTasksByCategory(category: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND isDone = 0 AND deadline <= :date")
    suspend fun getOverdueTasks(date: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("UPDATE tasks SET postponedCount = postponedCount + 1 WHERE id = :id")
    suspend fun incrementPostpone(id: Long)

    @Query("SELECT SUM(postponedCount) FROM tasks")
    fun getTotalPostponed(): LiveData<Int?>
}
