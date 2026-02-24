package com.srikar.lifeflow.ui.home

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
import com.google.android.material.tabs.TabLayout
import com.srikar.lifeflow.R
import com.srikar.lifeflow.data.entity.Chore
import com.srikar.lifeflow.data.entity.GroceryItem
import com.srikar.lifeflow.ui.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var choreAdapter: ChoreAdapter
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var rvChores: RecyclerView
    private lateinit var rvGrocery: RecyclerView
    private lateinit var grocerySection: View
    private lateinit var choreSection: View
    private var currentSubTab = 0  // 0 = chores, 1 = grocery

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choreSection = view.findViewById(R.id.chore_section)
        grocerySection = view.findViewById(R.id.grocery_section)
        rvChores = view.findViewById(R.id.rv_chores)
        rvGrocery = view.findViewById(R.id.rv_grocery)

        choreAdapter = ChoreAdapter(
            onToggle = { viewModel.toggleChore(it) },
            onDelete = { viewModel.deleteChore(it) }
        )
        rvChores.layoutManager = LinearLayoutManager(requireContext())
        rvChores.adapter = choreAdapter

        groceryAdapter = GroceryAdapter(
            onToggle = { viewModel.toggleGrocery(it) },
            onDelete = { viewModel.deleteGrocery(it) }
        )
        rvGrocery.layoutManager = LinearLayoutManager(requireContext())
        rvGrocery.adapter = groceryAdapter

        // Quick add grocery
        val etGrocery = view.findViewById<EditText>(R.id.et_grocery_quick)
        val btnAddGrocery = view.findViewById<Button>(R.id.btn_add_grocery)
        btnAddGrocery.setOnClickListener {
            val name = etGrocery.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.addGroceryItem(name)
                etGrocery.text.clear()
            }
        }

        val btnClearBought = view.findViewById<Button>(R.id.btn_clear_bought)
        btnClearBought.setOnClickListener { viewModel.clearBoughtGrocery() }

        // Sub-tabs
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_home_sub)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentSubTab = tab.position
                updateSubTabVisibility()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewModel.allChores.observe(viewLifecycleOwner) { choreAdapter.submitList(it) }
        viewModel.allGrocery.observe(viewLifecycleOwner) { groceryAdapter.submitList(it) }

        updateSubTabVisibility()
    }

    private fun updateSubTabVisibility() {
        choreSection.visibility = if (currentSubTab == 0) View.VISIBLE else View.GONE
        grocerySection.visibility = if (currentSubTab == 1) View.VISIBLE else View.GONE
    }

    fun showAddDialog() {
        if (currentSubTab == 0) showAddChoreDialog() else showAddGroceryDialog()
    }

    private fun showAddChoreDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_chore, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_chore_name)
        val spRoom = dialogView.findViewById<Spinner>(R.id.sp_room)
        val spFreq = dialogView.findViewById<Spinner>(R.id.sp_frequency)

        val rooms = arrayOf("Kitchen", "Living Room", "Bedroom", "Bathroom", "Other")
        spRoom.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, rooms)

        val freqs = arrayOf("Daily", "Weekly", "Biweekly", "Monthly")
        spFreq.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, freqs)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Chore")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addChore(name, spRoom.selectedItem.toString(), spFreq.selectedItem.toString().lowercase())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddGroceryDialog() {
        val et = EditText(requireContext()).apply { hint = "Item name" }
        AlertDialog.Builder(requireContext())
            .setTitle("Add Grocery Item")
            .setView(et)
            .setPositiveButton("Add") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isNotEmpty()) viewModel.addGroceryItem(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ─── Chore Adapter ─────────────────────────────────────────────────
class ChoreAdapter(
    private val onToggle: (Chore) -> Unit,
    private val onDelete: (Chore) -> Unit
) : RecyclerView.Adapter<ChoreAdapter.VH>() {

    private var items: List<Chore> = emptyList()
    fun submitList(list: List<Chore>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chore, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val cbDone: CheckBox = view.findViewById(R.id.cb_done)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvRoom: TextView = view.findViewById(R.id.tv_room)
        private val tvFreq: TextView = view.findViewById(R.id.tv_frequency)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(chore: Chore) {
            cbDone.isChecked = chore.isDone
            cbDone.setOnClickListener { onToggle(chore) }
            tvName.text = chore.name
            tvRoom.text = chore.room
            tvFreq.text = chore.frequency.replaceFirstChar { it.uppercase() }
            btnDelete.setOnClickListener { onDelete(chore) }
            itemView.alpha = if (chore.isDone) 0.5f else 1.0f
        }
    }
}

// ─── Grocery Adapter ───────────────────────────────────────────────
class GroceryAdapter(
    private val onToggle: (GroceryItem) -> Unit,
    private val onDelete: (GroceryItem) -> Unit
) : RecyclerView.Adapter<GroceryAdapter.VH>() {

    private var items: List<GroceryItem> = emptyList()
    fun submitList(list: List<GroceryItem>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grocery, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val cbBought: CheckBox = view.findViewById(R.id.cb_bought)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(item: GroceryItem) {
            cbBought.isChecked = item.isBought
            cbBought.setOnClickListener { onToggle(item) }
            tvName.text = item.name
            tvName.paintFlags = if (item.isBought) {
                tvName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
