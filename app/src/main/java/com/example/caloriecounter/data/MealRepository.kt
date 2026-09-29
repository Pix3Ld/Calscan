package com.example.caloriecounter.data

import kotlinx.coroutines.flow.Flow

class MealRepository(private val dao: MealDao) {
    fun observeSummaries(): Flow<List<MealSummaryEntity>> = dao.observeSummaries()

    fun observePhotoHistory(): Flow<List<PhotoHistoryEntity>> = dao.observePhotoHistory()

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

    suspend fun enqueuePhoto(imageBytes: ByteArray): Long {
        require(imageBytes.isNotEmpty()) { "Image cannot be empty" }
        return dao.insertJob(AnalysisJobEntity(inputType = InputType.PHOTO, imageBytes = imageBytes))
    }

    suspend fun deleteSummary(summaryId: Long) = dao.deleteSummary(summaryId)
}
