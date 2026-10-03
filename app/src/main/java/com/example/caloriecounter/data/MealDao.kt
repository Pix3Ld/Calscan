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

    @Query("SELECT * FROM analysis_jobs WHERE photoHistoryId = :photoId ORDER BY createdAt DESC LIMIT 1")
    fun observeJobForPhoto(photoId: Long): Flow<AnalysisJobEntity?>

    @Update
    suspend fun updateJob(job: AnalysisJobEntity)

    @Insert
    suspend fun insertSummary(summary: MealSummaryEntity)

    @Query("SELECT * FROM meal_summaries ORDER BY createdAt DESC")
    fun observeSummaries(): Flow<List<MealSummaryEntity>>

    @Query("SELECT * FROM meal_summaries WHERE jobId = :jobId LIMIT 1")
    fun observeSummaryForJob(jobId: Long): Flow<MealSummaryEntity?>

    @Query("DELETE FROM meal_summaries WHERE id = :summaryId")
    suspend fun deleteSummary(summaryId: Long)

    @Insert
    suspend fun insertPhotoHistory(photo: PhotoHistoryEntity): Long

    @Query("SELECT * FROM photo_history ORDER BY createdAt DESC")
    fun observePhotoHistory(): Flow<List<PhotoHistoryEntity>>

    @Query("DELETE FROM photo_history WHERE id = :photoId")
    suspend fun deletePhotoHistory(photoId: Long)

    @Query("DELETE FROM meal_summaries WHERE jobId IN (SELECT id FROM analysis_jobs WHERE photoHistoryId = :photoId)")
    suspend fun deleteSummariesForPhoto(photoId: Long)

    @Query("DELETE FROM analysis_jobs WHERE photoHistoryId = :photoId")
    suspend fun deleteJobsForPhoto(photoId: Long)
}
