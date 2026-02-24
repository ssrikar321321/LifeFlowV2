package com.srikar.lifeflow.ui.work

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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.srikar.lifeflow.R
import com.srikar.lifeflow.data.entity.Task
import com.srikar.lifeflow.ui.WorkViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WorkFragment : Fragment() {

    private val viewModel: WorkViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private lateinit var tvActive: TextView
    private lateinit var tvDone: TextView
    private lateinit var tvPostponed: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvActive = view.findViewById(R.id.tv_active_count)
        tvDone = view.findViewById(R.id.tv_done_count)
        tvPostponed = view.findViewById(R.id.tv_postponed_count)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_tasks)
        adapter = TaskAdapter(
            onToggle = { viewModel.toggleTask(it) },
            onPostpone = { viewModel.postponeTask(it) },
            onDelete = { viewModel.deleteTask(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_filter)
        val filters = listOf("All", "Active", "Done", "UKAEA Prep", "Research", "Admin", "Personal", "Learning")
        filters.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                setOnClickListener { viewModel.setFilter(label.lowercase()) }
            }
            chipGroup.addView(chip)
        }

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val filter = viewModel.filter.value ?: "all"
            val filtered = when (filter) {
                "all" -> tasks
                "active" -> tasks.filter { !it.isDone }
                "done" -> tasks.filter { it.isDone }
                else -> tasks.filter { it.category.equals(filter, ignoreCase = true) }
            }
            adapter.submitList(filtered)
            tvActive.text = tasks.count { !it.isDone }.toString()
            tvDone.text = tasks.count { it.isDone }.toString()
        }

        viewModel.filter.observe(viewLifecycleOwner) {
            viewModel.allTasks.value?.let { tasks ->
                val filtered = when (it) {
                    "all" -> tasks
                    "active" -> tasks.filter { t -> !t.isDone }
                    "done" -> tasks.filter { t -> t.isDone }
                    else -> tasks.filter { t -> t.category.equals(it, ignoreCase = true) }
                }
                adapter.submitList(filtered)
            }
        }

        viewModel.totalPostponed.observe(viewLifecycleOwner) { count ->
            tvPostponed.text = (count ?: 0).toString()
        }
    }

    fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_task_title)
        val spCategory = dialogView.findViewById<Spinner>(R.id.sp_category)
        val spPriority = dialogView.findViewById<Spinner>(R.id.sp_priority)
        val etDeadline = dialogView.findViewById<EditText>(R.id.et_deadline)

        val categories = arrayOf("UKAEA Prep", "Research", "Admin", "Personal", "Learning")
        spCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)

        val priorities = arrayOf("High", "Medium", "Low")
        spPriority.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, priorities)
        spPriority.setSelection(1) // default medium

        AlertDialog.Builder(requireContext())
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.addTask(
                        title = title,
                        category = spCategory.selectedItem.toString(),
                        priority = spPriority.selectedItem.toString().lowercase(),
                        deadline = etDeadline.text.toString().trim().ifBlank { null }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ─── Task Adapter ──────────────────────────────────────────────────
class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onPostpone: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    private var items: List<Task> = emptyList()

    fun submitList(list: List<Task>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val cbDone: CheckBox = view.findViewById(R.id.cb_done)
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvCategory: TextView = view.findViewById(R.id.tv_category)
        private val tvPriority: TextView = view.findViewById(R.id.tv_priority)
        private val tvDeadline: TextView = view.findViewById(R.id.tv_deadline)
        private val tvPostponed: TextView = view.findViewById(R.id.tv_postponed)
        private val btnPostpone: ImageButton = view.findViewById(R.id.btn_postpone)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(task: Task) {
            cbDone.isChecked = task.isDone
            cbDone.setOnClickListener { onToggle(task) }

            tvTitle.text = task.title
            tvTitle.paintFlags = if (task.isDone) {
                tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            tvCategory.text = task.category
            tvPriority.text = task.priority.replaceFirstChar { it.uppercase() }

            if (task.deadline != null) {
                try {
                    val deadline = LocalDate.parse(task.deadline)
                    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline)
                    tvDeadline.text = when {
                        task.isDone -> formatDate(task.deadline)
                        daysLeft < 0 -> "${-daysLeft}d overdue"
                        daysLeft == 0L -> "Today!"
                        else -> "${daysLeft}d left"
                    }
                    tvDeadline.visibility = View.VISIBLE
                } catch (e: Exception) {
                    tvDeadline.text = task.deadline
                    tvDeadline.visibility = View.VISIBLE
                }
            } else {
                tvDeadline.visibility = View.GONE
            }

            if (task.postponedCount > 0) {
                tvPostponed.visibility = View.VISIBLE
                tvPostponed.text = when {
                    task.postponedCount >= 5 -> "🚨 x${task.postponedCount} - Stop avoiding!"
                    task.postponedCount >= 3 -> "⚠️ x${task.postponedCount} - Really?"
                    else -> "⏰ x${task.postponedCount}"
                }
            } else {
                tvPostponed.visibility = View.GONE
            }

            btnPostpone.visibility = if (task.isDone) View.GONE else View.VISIBLE
            btnPostpone.setOnClickListener { onPostpone(task) }
            btnDelete.setOnClickListener { onDelete(task) }

            itemView.alpha = if (task.isDone) 0.5f else 1.0f
        }

        private fun formatDate(d: String): String {
            return try {
                val date = LocalDate.parse(d)
                date.format(DateTimeFormatter.ofPattern("d MMM"))
            } catch (e: Exception) { d }
        }
    }
}
