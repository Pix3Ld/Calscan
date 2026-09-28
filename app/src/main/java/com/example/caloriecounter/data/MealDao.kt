package com.example.caloriecounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insertJob(job: AnalysisJobEntity): Long

    @Query("SELECT * FROM analysis_jobs WHERE id = :jobId")
    suspend fun getJob(jobId: Long): AnalysisJobEntity?

    @Update
    suspend fun updateJob(job: AnalysisJobEntity)

    @Insert
    suspend fun insertSummary(summary: MealSummaryEntity)

    @Query("SELECT * FROM meal_summaries ORDER BY createdAt DESC")
    fun observeSummaries(): Flow<List<MealSummaryEntity>>

    @Query("DELETE FROM meal_summaries WHERE id = :summaryId")
    suspend fun deleteSummary(summaryId: Long)

    @Insert
    suspend fun insertPhotoHistory(photo: PhotoHistoryEntity): Long

    @Query("SELECT * FROM photo_history ORDER BY createdAt DESC")
    fun observePhotoHistory(): Flow<List<PhotoHistoryEntity>>

    @Query("DELETE FROM photo_history WHERE id = :photoId")
    suspend fun deletePhotoHistory(photoId: Long)
}
