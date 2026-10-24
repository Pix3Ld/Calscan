package com.example.caloriecounter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Mapuje enumy Kotlin na tekst w SQLite i z powrotem, aby Room mógł je zapisać.
class DatabaseConverters {
    @TypeConverter
    fun inputTypeToString(value: InputType): String = value.name

    @TypeConverter
    fun stringToInputType(value: String): InputType = InputType.valueOf(value)

    @TypeConverter
    fun jobStatusToString(value: JobStatus): String = value.name

    @TypeConverter
    fun stringToJobStatus(value: String): JobStatus = JobStatus.valueOf(value)
}

@Database(
    entities = [AnalysisJobEntity::class, MealSummaryEntity::class, PhotoHistoryEntity::class, ActivitySessionEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    // Zachowuje istniejące dane podczas dodawania historii zdjęć i powiązań z analizą.
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `photo_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `imageBytes` BLOB NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL)"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `analysis_jobs` ADD COLUMN `photoHistoryId` INTEGER")
                database.execSQL("ALTER TABLE `analysis_jobs` ADD COLUMN `analysisNote` TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `activity_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `activityType` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `distanceMeters` REAL NOT NULL, `steps` INTEGER NOT NULL, `caloriesKcal` INTEGER NOT NULL, `routeJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"""
                )
            }
        }
    }
}
