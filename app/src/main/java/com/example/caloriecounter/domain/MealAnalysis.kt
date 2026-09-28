package com.example.caloriecounter.domain

import com.example.caloriecounter.data.InputType

interface MealAnalyzer {
    suspend fun analyze(input: AnalysisInput): AnalysisResult
}

data class AnalysisInput(
    val type: InputType,
    val text: String?,
    val imageBytes: ByteArray?
)

data class AnalysisResult(
    val title: String,
    val caloriesKcal: Int,
    val proteinGrams: Float,
    val carbohydratesGrams: Float,
    val fatGrams: Float,
    val fiberGrams: Float,
    val vitaminsJson: String
)

class GemmaMealAnalyzer : MealAnalyzer {
    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        error("Gemma adapter is not configured yet")
    }
}
