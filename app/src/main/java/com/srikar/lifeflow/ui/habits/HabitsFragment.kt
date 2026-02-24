package com.srikar.lifeflow.ui.habits

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.srikar.lifeflow.R
import com.srikar.lifeflow.data.entity.Habit
import com.srikar.lifeflow.data.entity.HabitLog
import com.srikar.lifeflow.ui.HabitsViewModel

class HabitsFragment : Fragment() {

    private val viewModel: HabitsViewModel by viewModels()
    private lateinit var adapter: HabitAdapter
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvProgress = view.findViewById(R.id.tv_progress)
        progressBar = view.findViewById(R.id.progress_bar)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_habits)
        adapter = HabitAdapter(
            onToggle = { viewModel.toggleHabit(it.id) },
            onDelete = { viewModel.deleteHabit(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        var currentLogs: Set<Long> = emptySet()

        viewModel.todayLogs.observe(viewLifecycleOwner) { logs ->
            currentLogs = logs.filter { it.completed }.map { it.habitId }.toSet()
            adapter.updateLogs(currentLogs)
        }

        viewModel.allHabits.observe(viewLifecycleOwner) { habits ->
            adapter.submitList(habits)
            updateProgress(habits.size)
        }

        viewModel.completedToday.observe(viewLifecycleOwner) { count ->
            val total = viewModel.allHabits.value?.size ?: 1
            updateProgressValues(count, total)
        }
    }

    private fun updateProgress(total: Int) {
        val completed = viewModel.completedToday.value ?: 0
        updateProgressValues(completed, total)
    }

    private fun updateProgressValues(completed: Int, total: Int) {
        val pct = if (total > 0) (completed * 100) / total else 0
        tvProgress.text = "$completed/$total completed ($pct%)"
        progressBar.progress = pct
    }

    fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_habit_name)
        val etTime = dialogView.findViewById<EditText>(R.id.et_reminder_time)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addHabit(
                        name = name,
                        icon = "✨",
                        reminderTime = etTime.text.toString().ifBlank { "08:00" }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ─── Habit Adapter ─────────────────────────────────────────────────
class HabitAdapter(
    private val onToggle: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.VH>() {

    private var items: List<Habit> = emptyList()
    private var completedIds: Set<Long> = emptySet()

    fun submitList(list: List<Habit>) {
        items = list
        notifyDataSetChanged()
    }

    fun updateLogs(ids: Set<Long>) {
        completedIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvIcon: TextView = view.findViewById(R.id.tv_icon)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvStreak: TextView = view.findViewById(R.id.tv_streak)
        private val tvBest: TextView = view.findViewById(R.id.tv_best)
        private val btnCheck: Button = view.findViewById(R.id.btn_check)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(habit: Habit) {
            val done = completedIds.contains(habit.id)
            tvIcon.text = if (done) "✅" else habit.icon
            tvName.text = habit.name
            tvStreak.text = "🔥 ${habit.currentStreak}d"
            tvBest.text = "Best: ${habit.bestStreak}d"

            btnCheck.text = if (done) "Done ✓" else "Mark Done"
            btnCheck.setOnClickListener { onToggle(habit) }
            btnDelete.setOnClickListener { onDelete(habit) }

            itemView.alpha = if (done) 0.65f else 1.0f
        }
    }
}
