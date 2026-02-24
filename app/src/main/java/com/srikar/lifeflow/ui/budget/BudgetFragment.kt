package com.srikar.lifeflow.ui.budget

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
import com.srikar.lifeflow.data.entity.BudgetEntry
import com.srikar.lifeflow.ui.BudgetViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetFragment : Fragment() {

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: BudgetAdapter
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvBalance: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvIncome = view.findViewById(R.id.tv_income)
        tvExpense = view.findViewById(R.id.tv_expense)
        tvBalance = view.findViewById(R.id.tv_balance)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_entries)
        adapter = BudgetAdapter { viewModel.deleteEntry(it) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.allEntries.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.totalIncome.observe(viewLifecycleOwner) { updateSummary() }
        viewModel.totalExpenses.observe(viewLifecycleOwner) { updateSummary() }
    }

    private fun updateSummary() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpenses.value ?: 0.0
        val balance = income - expense

        tvIncome.text = "€${String.format("%.0f", income)}"
        tvExpense.text = "€${String.format("%.0f", expense)}"
        tvBalance.text = "€${String.format("%.0f", balance)}"
    }

    fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val spCategory = dialogView.findViewById<Spinner>(R.id.sp_category)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rg_type)

        val expenseCats = arrayOf("Housing", "Food", "Transport", "Utilities", "Health", "Entertainment", "Savings", "Debt", "Other")
        val incomeCats = arrayOf("Salary", "Overtime", "Freelance", "Other")

        spCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, expenseCats)

        rgType.setOnCheckedChangeListener { _, id ->
            val cats = if (id == R.id.rb_income) incomeCats else expenseCats
            spCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cats)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Entry")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull()
                if (name.isNotEmpty() && amount != null && amount > 0) {
                    val type = if (rgType.checkedRadioButtonId == R.id.rb_income) "income" else "expense"
                    viewModel.addEntry(name, amount, spCategory.selectedItem.toString(), type)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ─── Budget Adapter ────────────────────────────────────────────────
class BudgetAdapter(
    private val onDelete: (BudgetEntry) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.VH>() {

    private var items: List<BudgetEntry> = emptyList()
    fun submitList(list: List<BudgetEntry>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvIcon: TextView = view.findViewById(R.id.tv_icon)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvCategory: TextView = view.findViewById(R.id.tv_category)
        private val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(entry: BudgetEntry) {
            tvIcon.text = if (entry.type == "income") "📈" else "📉"
            tvName.text = entry.name
            tvCategory.text = "${entry.category} · ${formatDate(entry.date)}"
            tvAmount.text = "${if (entry.type == "income") "+" else "-"}€${String.format("%.0f", entry.amount)}"
            btnDelete.setOnClickListener { onDelete(entry) }
        }

        private fun formatDate(d: String): String {
            return try {
                val date = LocalDate.parse(d)
                date.format(DateTimeFormatter.ofPattern("d MMM"))
            } catch (e: Exception) { d }
        }
    }
}
