package com.example.caloriecounter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

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
    entities = [AnalysisJobEntity::class, MealSummaryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
