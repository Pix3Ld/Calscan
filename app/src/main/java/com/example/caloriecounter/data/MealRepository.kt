package com.example.caloriecounter.data

import kotlinx.coroutines.flow.Flow

class MealRepository(private val dao: MealDao) {
    fun observeSummaries(): Flow<List<MealSummaryEntity>> = dao.observeSummaries()

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
