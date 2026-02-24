package com.srikar.lifeflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.srikar.lifeflow.ui.work.WorkFragment
import com.srikar.lifeflow.ui.habits.HabitsFragment
import com.srikar.lifeflow.ui.home.HomeFragment
import com.srikar.lifeflow.ui.budget.BudgetFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private var currentFragmentTag: String = TAG_WORK

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()

        bottomNav = findViewById(R.id.bottom_navigation)
        fabAdd = findViewById(R.id.fab_add)

        if (savedInstanceState == null) {
            loadFragment(WorkFragment(), TAG_WORK)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_work -> { loadFragment(WorkFragment(), TAG_WORK); true }
                R.id.nav_habits -> { loadFragment(HabitsFragment(), TAG_HABITS); true }
                R.id.nav_home -> { loadFragment(HomeFragment(), TAG_HOME); true }
                R.id.nav_budget -> { loadFragment(BudgetFragment(), TAG_BUDGET); true }
                else -> false
            }
        }

        fabAdd.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            when (currentFragment) {
                is WorkFragment -> currentFragment.showAddDialog()
                is HabitsFragment -> currentFragment.showAddDialog()
                is HomeFragment -> currentFragment.showAddDialog()
                is BudgetFragment -> currentFragment.showAddDialog()
            }
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        currentFragmentTag = tag
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        const val TAG_WORK = "work"
        const val TAG_HABITS = "habits"
        const val TAG_HOME = "home"
        const val TAG_BUDGET = "budget"
    }
}
