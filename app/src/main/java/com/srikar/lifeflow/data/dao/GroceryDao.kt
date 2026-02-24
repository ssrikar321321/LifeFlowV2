package com.srikar.lifeflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srikar.lifeflow.data.entity.GroceryItem

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_items ORDER BY isBought ASC, addedAt DESC")
    fun getAllItems(): LiveData<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryItem): Long

    @Update
    suspend fun update(item: GroceryItem)

    @Delete
    suspend fun delete(item: GroceryItem)

    @Query("UPDATE grocery_items SET isBought = :bought WHERE id = :id")
    suspend fun setBought(id: Long, bought: Boolean)

    @Query("DELETE FROM grocery_items WHERE isBought = 1")
    suspend fun clearBought()
}
