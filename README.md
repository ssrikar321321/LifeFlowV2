# LifeFlow - Personal Organization App

A comprehensive Android app designed to help manage work tasks, daily habits, home chores, grocery shopping, and budgeting — built specifically for productivity and anti-procrastination.

## Features

### 📋 Work Tasks
- Task management with categories: UKAEA Prep, Research, Admin, Personal, Learning
- Priority levels (High/Medium/Low) with color coding
- Deadline tracking with overdue alerts
- **Postpone counter with guilt messages** — tracks how many times you postpone each task
- Filter by status or category

### ✅ Daily Habits  
- Streak tracking (current + best streak)
- Daily check-off with progress bar
- Custom reminder times per habit
- Visual progress percentage

### 🏠 Home Management
- **Chores** organized by room (Kitchen, Living Room, Bedroom, Bathroom)
- Frequency tracking (daily, weekly, biweekly, monthly)
- **Grocery list** with quick-add, check-off, and clear bought items

### 💰 Budget Tracker
- Income vs expense tracking
- Category breakdown with visual bars
- Running balance calculation
- Categories: Housing, Food, Transport, Utilities, Health, Entertainment, Savings, Debt

## Architecture

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite) with KSP annotation processing
- **Async**: Kotlin Coroutines + LiveData
- **Notifications**: WorkManager with daily reminders
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

## Setup

1. Open the project in **Android Studio Hedgehog (2023.1.1)** or newer
2. Sync Gradle (should auto-sync on import)
3. Run on emulator or physical device (API 26+)

## Project Structure

```
app/src/main/
├── java/com/srikar/lifeflow/
│   ├── LifeFlowApp.kt              # Application class
│   ├── MainActivity.kt              # Main activity with bottom nav
│   ├── data/
│   │   ├── entity/                  # Room entities (Task, Habit, etc.)
│   │   ├── dao/                     # Data Access Objects
│   │   ├── database/                # Room database
│   │   └── repository/              # Repository layer
│   ├── ui/
│   │   ├── ViewModels.kt           # All ViewModels
│   │   ├── work/WorkFragment.kt    # Work tasks UI
│   │   ├── habits/HabitsFragment.kt
│   │   ├── home/HomeFragment.kt
│   │   └── budget/BudgetFragment.kt
│   └── util/
│       └── ReminderWorker.kt       # Notification scheduling
└── res/
    ├── layout/                      # All XML layouts
    ├── menu/                        # Bottom navigation menu
    ├── drawable/                    # Card backgrounds, icons
    └── values/                      # Colors, themes, strings
```

## Key Bug Fixes (vs. previous version)

1. **Room KSP vs KAPT**: Uses `ksp` instead of `kapt` for Room compiler — fixes annotation processing crashes
2. **Proper LiveData observation**: All fragments observe LiveData in `viewLifecycleOwner` scope — prevents memory leaks
3. **Foreign key cascade**: HabitLog has proper `onDelete = CASCADE` — no orphaned records
4. **Null safety**: All nullable fields properly handled with `?.` and `?:` operators
5. **Thread safety**: Database instance uses `@Volatile` + `synchronized` double-check locking
6. **Streak calculation**: Uses `LocalDate` API (not string comparison) for reliable date math
7. **Fragment recreation**: ViewModels survive configuration changes — no data loss on rotation

## Building APK

### In Android Studio:
Build > Build Bundle(s) / APK(s) > Build APK(s)

### Via GitHub Actions (free CI/CD):
Push to GitHub and add `.github/workflows/build.yml` — see previous discussion for workflow file.

## License
Personal use — built by Srikar for Srikar ✌️
