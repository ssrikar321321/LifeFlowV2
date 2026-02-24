package com.srikar.lifeflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srikar.lifeflow.data.entity.BudgetEntry

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budget_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): LiveData<List<BudgetEntry>>

    @Query("SELECT * FROM budget_entries WHERE type = 'expense' ORDER BY date DESC")
    fun getExpenses(): LiveData<List<BudgetEntry>>

    @Query("SELECT * FROM budget_entries WHERE type = 'income' ORDER BY date DESC")
    fun getIncomes(): LiveData<List<BudgetEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM budget_entries WHERE type = 'expense'")
    fun getTotalExpenses(): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM budget_entries WHERE type = 'income'")
    fun getTotalIncome(): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM budget_entries WHERE type = 'expense' AND category = :category")
    fun getExpenseByCategory(category: String): LiveData<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BudgetEntry): Long

    @Update
    suspend fun update(entry: BudgetEntry)

    @Delete
    suspend fun delete(entry: BudgetEntry)
}
