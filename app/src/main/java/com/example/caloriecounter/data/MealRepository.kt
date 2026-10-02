package com.example.caloriecounter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class PhotoAnalysisDetails(
    val job: AnalysisJobEntity? = null,
    val summary: MealSummaryEntity? = null
)

class MealRepository(private val dao: MealDao) {
    fun observeSummaries(): Flow<List<MealSummaryEntity>> = dao.observeSummaries()

    fun observePhotoHistory(): Flow<List<PhotoHistoryEntity>> = dao.observePhotoHistory()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAnalysisForPhoto(photoId: Long): Flow<PhotoAnalysisDetails> =
        dao.observeJobForPhoto(photoId).flatMapLatest { job ->
            if (job == null) {
                flowOf(PhotoAnalysisDetails())
            } else {
                dao.observeSummaryForJob(job.id).map { summary -> PhotoAnalysisDetails(job, summary) }
            }
        }

    suspend fun savePhoto(imageBytes: ByteArray, note: String?): PhotoHistoryEntity {
        require(imageBytes.isNotEmpty()) { "Image cannot be empty" }
        val photo = PhotoHistoryEntity(
            imageBytes = imageBytes,
            note = note?.trim()?.takeIf { it.isNotEmpty() }
        )
        return photo.copy(id = dao.insertPhotoHistory(photo))
    }

    suspend fun deletePhoto(photoId: Long) = dao.deletePhotoHistory(photoId)

    suspend fun enqueueText(text: String): Long {
        require(text.isNotBlank()) { "Text input cannot be blank" }
        return dao.insertJob(AnalysisJobEntity(inputType = InputType.TEXT, textInput = text.trim()))
    }

    suspend fun enqueuePhoto(imageBytes: ByteArray, photoId: Long? = null, note: String? = null): Long {
        require(imageBytes.isNotEmpty()) { "Image cannot be empty" }
        return dao.insertJob(
            AnalysisJobEntity(
                inputType = InputType.PHOTO,
                imageBytes = imageBytes,
                photoHistoryId = photoId,
                analysisNote = note?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    suspend fun deleteSummary(summaryId: Long) = dao.deleteSummary(summaryId)
}
