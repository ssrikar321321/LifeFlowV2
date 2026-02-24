package com.srikar.lifeflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srikar.lifeflow.data.entity.Chore

@Dao
interface ChoreDao {

    @Query("SELECT * FROM chores ORDER BY isDone ASC, room ASC, name ASC")
    fun getAllChores(): LiveData<List<Chore>>

    @Query("SELECT * FROM chores WHERE room = :room ORDER BY isDone ASC, name ASC")
    fun getChoresByRoom(room: String): LiveData<List<Chore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chore: Chore): Long

    @Update
    suspend fun update(chore: Chore)

    @Delete
    suspend fun delete(chore: Chore)

    @Query("UPDATE chores SET isDone = :done, lastDoneDate = :date WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, date: String?)

    @Query("UPDATE chores SET isDone = 0 WHERE frequency = :frequency")
    suspend fun resetByFrequency(frequency: String)
}
