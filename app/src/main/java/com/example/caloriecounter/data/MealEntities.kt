package com.example.caloriecounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_jobs")
data class AnalysisJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inputType: InputType,
    val textInput: String? = null,
    val imageBytes: ByteArray? = null,
    val status: JobStatus = JobStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)

@Entity(tableName = "meal_summaries")
data class MealSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val title: String,
    val caloriesKcal: Int,
    val proteinGrams: Float,
    val carbohydratesGrams: Float,
    val fatGrams: Float,
    val fiberGrams: Float,
    val vitaminsJson: String,
    val imageBytes: ByteArray?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_history")
data class PhotoHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageBytes: ByteArray,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class InputType { PHOTO, TEXT }
enum class JobStatus { QUEUED, PROCESSING, COMPLETED, FAILED }
