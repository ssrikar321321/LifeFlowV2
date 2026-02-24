package com.srikar.lifeflow.ui

import android.app.Application
import androidx.lifecycle.*
import com.srikar.lifeflow.LifeFlowApp
import com.srikar.lifeflow.data.entity.*
import com.srikar.lifeflow.data.repository.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// ─── Work ViewModel ────────────────────────────────────────────────
class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: TaskRepository

    val allTasks: LiveData<List<Task>>
    val activeTasks: LiveData<List<Task>>
    val completedTasks: LiveData<List<Task>>
    val totalPostponed: LiveData<Int?>

    private val _filter = MutableLiveData("all")
    val filter: LiveData<String> = _filter

    init {
        val dao = (application as LifeFlowApp).database.taskDao()
        repo = TaskRepository(dao)
        allTasks = repo.allTasks
        activeTasks = repo.activeTasks
        completedTasks = repo.completedTasks
        totalPostponed = repo.totalPostponed
    }

    fun setFilter(f: String) { _filter.value = f }

    fun getByCategory(cat: String) = repo.getByCategory(cat)

    fun addTask(title: String, category: String, priority: String, deadline: String?) {
        viewModelScope.launch {
            repo.insert(Task(
                title = title,
                category = category,
                priority = priority,
                deadline = if (deadline.isNullOrBlank()) null else deadline,
                createdAt = LocalDate.now().toString()
            ))
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch { repo.toggleDone(task.id, !task.isDone) }
    }

    fun postponeTask(task: Task) {
        viewModelScope.launch { repo.postpone(task.id) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repo.delete(task) }
    }
}

// ─── Habits ViewModel ──────────────────────────────────────────────
class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: HabitRepository
    val allHabits: LiveData<List<Habit>>

    private val todayStr = LocalDate.now().toString()
    val todayLogs: LiveData<List<HabitLog>>
    val completedToday: LiveData<Int>

    init {
        val dao = (application as LifeFlowApp).database.habitDao()
        repo = HabitRepository(dao)
        allHabits = repo.allHabits
        todayLogs = repo.getLogsForDate(todayStr)
        completedToday = repo.getCompletedCount(todayStr)
    }

    fun addHabit(name: String, icon: String, reminderTime: String) {
        viewModelScope.launch {
            repo.insertHabit(Habit(
                name = name,
                icon = icon,
                reminderTime = reminderTime,
                createdAt = LocalDate.now().toString()
            ))
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch { repo.toggleHabit(habitId, todayStr) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repo.deleteHabit(habit) }
    }
}

// ─── Home ViewModel ────────────────────────────────────────────────
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val choreRepo: ChoreRepository
    private val groceryRepo: GroceryRepository

    val allChores: LiveData<List<Chore>>
    val allGrocery: LiveData<List<GroceryItem>>

    init {
        val db = (application as LifeFlowApp).database
        choreRepo = ChoreRepository(db.choreDao())
        groceryRepo = GroceryRepository(db.groceryDao())
        allChores = choreRepo.allChores
        allGrocery = groceryRepo.allItems
    }

    fun addChore(name: String, room: String, frequency: String) {
        viewModelScope.launch {
            choreRepo.insert(Chore(name = name, room = room, frequency = frequency))
        }
    }

    fun toggleChore(chore: Chore) {
        viewModelScope.launch { choreRepo.toggleDone(chore.id, !chore.isDone) }
    }

    fun deleteChore(chore: Chore) {
        viewModelScope.launch { choreRepo.delete(chore) }
    }

    fun addGroceryItem(name: String) {
        viewModelScope.launch {
            groceryRepo.insert(GroceryItem(
                name = name,
                addedAt = LocalDate.now().toString()
            ))
        }
    }

    fun toggleGrocery(item: GroceryItem) {
        viewModelScope.launch { groceryRepo.toggleBought(item.id, !item.isBought) }
    }

    fun deleteGrocery(item: GroceryItem) {
        viewModelScope.launch { groceryRepo.delete(item) }
    }

    fun clearBoughtGrocery() {
        viewModelScope.launch { groceryRepo.clearBought() }
    }
}

// ─── Budget ViewModel ──────────────────────────────────────────────
class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: BudgetRepository

    val allEntries: LiveData<List<BudgetEntry>>
    val totalExpenses: LiveData<Double>
    val totalIncome: LiveData<Double>

    init {
        val dao = (application as LifeFlowApp).database.budgetDao()
        repo = BudgetRepository(dao)
        allEntries = repo.allEntries
        totalExpenses = repo.totalExpenses
        totalIncome = repo.totalIncome
    }

    fun addEntry(name: String, amount: Double, category: String, type: String) {
        viewModelScope.launch {
            repo.insert(BudgetEntry(
                name = name,
                amount = amount,
                category = category,
                type = type,
                date = LocalDate.now().toString()
            ))
        }
    }

    fun deleteEntry(entry: BudgetEntry) {
        viewModelScope.launch { repo.delete(entry) }
    }
}
