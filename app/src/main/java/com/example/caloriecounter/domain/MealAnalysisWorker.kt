package com.example.caloriecounter.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.caloriecounter.data.DatabaseProvider
import com.example.caloriecounter.data.JobStatus
import com.example.caloriecounter.data.MealSummaryEntity
import java.io.File

class MealAnalysisWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private val dao = DatabaseProvider.get(appContext).mealDao()
    private val analyzer: MealAnalyzer = GemmaMealAnalyzer(
        modelPath = File(appContext.filesDir, "models/gemma-4-E4B-it.litertlm").absolutePath,
        cacheDirectory = appContext.cacheDir.absolutePath
    )

    override suspend fun doWork(): Result {
        val jobId = inputData.getLong(JOB_ID, -1L)
        val job = dao.getJob(jobId) ?: return Result.failure()
        dao.updateJob(job.copy(status = JobStatus.PROCESSING, updatedAt = System.currentTimeMillis()))

        return try {
            val result = analyzer.analyze(
                AnalysisInput(job.inputType, job.textInput, job.imageBytes, job.analysisNote)
            )
            dao.insertSummary(
                MealSummaryEntity(
                    jobId = job.id,
                    title = result.title,
                    caloriesKcal = result.caloriesKcal,
                    proteinGrams = result.proteinGrams,
                    carbohydratesGrams = result.carbohydratesGrams,
                    fatGrams = result.fatGrams,
                    fiberGrams = result.fiberGrams,
                    vitaminsJson = result.vitaminsJson,
                    imageBytes = job.imageBytes
                )
            )
            dao.updateJob(job.copy(status = JobStatus.COMPLETED, updatedAt = System.currentTimeMillis()))
            Result.success()
        } catch (error: Exception) {
            dao.updateJob(
                job.copy(
                    status = JobStatus.FAILED,
                    errorMessage = error.message?.takeIf { it.isNotBlank() } ?: error.toString(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.failure()
        }
    }

    companion object {
        const val JOB_ID = "job_id"
    }
}
