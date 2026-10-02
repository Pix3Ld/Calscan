package com.example.caloriecounter.domain

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.caloriecounter.data.DatabaseProvider
import com.example.caloriecounter.data.MealRepository
import java.util.concurrent.TimeUnit

class AnalysisQueue(context: Context) {
    private val appContext = context.applicationContext
    private val repository = MealRepository(DatabaseProvider.get(appContext).mealDao())
    private val workManager = WorkManager.getInstance(appContext)

    suspend fun enqueueText(text: String): Long = enqueue(repository.enqueueText(text))

    suspend fun enqueuePhoto(imageBytes: ByteArray, photoId: Long? = null, note: String? = null): Long =
        enqueue(repository.enqueuePhoto(imageBytes, photoId, note))

    private fun enqueue(jobId: Long): Long {
        val request = OneTimeWorkRequestBuilder<MealAnalysisWorker>()
            .setInputData(Data.Builder().putLong(MealAnalysisWorker.JOB_ID, jobId).build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "meal-analysis-$jobId",
            ExistingWorkPolicy.KEEP,
            request
        )
        return jobId
    }
}
