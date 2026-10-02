package com.example.caloriecounter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    entities = [AnalysisJobEntity::class, MealSummaryEntity::class, PhotoHistoryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

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
    }
}
