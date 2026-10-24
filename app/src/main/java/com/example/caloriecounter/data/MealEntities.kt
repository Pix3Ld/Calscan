package com.example.caloriecounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Rekord kolejki analizy: zawiera wejście, status, ewentualny błąd i znaczniki czasu.
@Entity(tableName = "analysis_jobs")
data class AnalysisJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inputType: InputType,
    val textInput: String? = null,
    val imageBytes: ByteArray? = null,
    val photoHistoryId: Long? = null,
    val analysisNote: String? = null,
    val status: JobStatus = JobStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)

// Wynik analizy powiązany z zadaniem, używany w podsumowaniach i szczegółach posiłku.
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

// Oryginalne zdjęcie i notatka użytkownika wyświetlane w historii posiłków.
@Entity(tableName = "photo_history")
data class PhotoHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageBytes: ByteArray,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val steps: Int,
    val caloriesKcal: Int,
    val routeJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Typ przesłanego materiału oraz status jego przetwarzania.
enum class InputType { PHOTO, TEXT }
enum class JobStatus { QUEUED, PROCESSING, COMPLETED, FAILED }
