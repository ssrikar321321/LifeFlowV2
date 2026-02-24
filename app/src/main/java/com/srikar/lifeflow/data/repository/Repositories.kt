package com.srikar.lifeflow.data.repository

import androidx.lifecycle.LiveData
import com.srikar.lifeflow.data.dao.*
import com.srikar.lifeflow.data.entity.*
import java.time.LocalDate

class TaskRepository(private val dao: TaskDao) {
    val allTasks: LiveData<List<Task>> = dao.getAllTasks()
    val activeTasks: LiveData<List<Task>> = dao.getActiveTasks()
    val completedTasks: LiveData<List<Task>> = dao.getCompletedTasks()
    val totalPostponed: LiveData<Int?> = dao.getTotalPostponed()

    fun getByCategory(cat: String) = dao.getTasksByCategory(cat)
    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun toggleDone(id: Long, done: Boolean) = dao.setDone(id, done)
    suspend fun postpone(id: Long) = dao.incrementPostpone(id)
}

class HabitRepository(private val dao: HabitDao) {
    val allHabits: LiveData<List<Habit>> = dao.getAllHabits()

    fun getLogsForDate(date: String) = dao.getLogsForDate(date)
    fun getCompletedCount(date: String) = dao.getCompletedCountForDate(date)

    suspend fun insertHabit(habit: Habit) = dao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    suspend fun toggleHabit(habitId: Long, date: String) {
        val existing = dao.getLog(habitId, date)
        if (existing != null) {
            dao.deleteLog(habitId, date)
        } else {
            dao.insertLog(HabitLog(habitId = habitId, date = date, completed = true))
        }
        recalculateStreak(habitId)
    }

    private suspend fun recalculateStreak(habitId: Long) {
        val logs = dao.getLogsForHabit(habitId)
        val logDates = logs.filter { it.completed }.map { it.date }.toSet()

        var streak = 0
        var d = LocalDate.now()
        while (logDates.contains(d.toString())) {
            streak++
            d = d.minusDays(1)
        }

        val habit = dao.getHabitById(habitId) ?: return
        val best = maxOf(habit.bestStreak, streak)
        dao.updateStreak(habitId, streak, best)
    }
}

class ChoreRepository(private val dao: ChoreDao) {
    val allChores: LiveData<List<Chore>> = dao.getAllChores()

    fun getByRoom(room: String) = dao.getChoresByRoom(room)
    suspend fun insert(chore: Chore) = dao.insert(chore)
    suspend fun update(chore: Chore) = dao.update(chore)
    suspend fun delete(chore: Chore) = dao.delete(chore)
    suspend fun toggleDone(id: Long, done: Boolean) {
        val date = if (done) LocalDate.now().toString() else null
        dao.setDone(id, done, date)
    }
    suspend fun resetByFrequency(freq: String) = dao.resetByFrequency(freq)
}

class GroceryRepository(private val dao: GroceryDao) {
    val allItems: LiveData<List<GroceryItem>> = dao.getAllItems()

    suspend fun insert(item: GroceryItem) = dao.insert(item)
    suspend fun update(item: GroceryItem) = dao.update(item)
    suspend fun delete(item: GroceryItem) = dao.delete(item)
    suspend fun toggleBought(id: Long, bought: Boolean) = dao.setBought(id, bought)
    suspend fun clearBought() = dao.clearBought()
}

class BudgetRepository(private val dao: BudgetDao) {
    val allEntries: LiveData<List<BudgetEntry>> = dao.getAllEntries()
    val totalExpenses: LiveData<Double> = dao.getTotalExpenses()
    val totalIncome: LiveData<Double> = dao.getTotalIncome()

    suspend fun insert(entry: BudgetEntry) = dao.insert(entry)
    suspend fun update(entry: BudgetEntry) = dao.update(entry)
    suspend fun delete(entry: BudgetEntry) = dao.delete(entry)
}
