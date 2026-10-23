package com.example.caloriecounter.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    // Tworzy bazę przy pierwszym użyciu, a kolejnym wywołaniom zwraca tę samą instancję.
    fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "calorie-counter.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build().also { instance = it }
    }
}
