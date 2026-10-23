package com.example.caloriecounter.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.caloriecounter.R
import com.example.caloriecounter.data.MealRepository
import com.example.caloriecounter.data.PhotoHistoryEntity
import com.example.caloriecounter.domain.AnalysisQueue
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

/**
 * Koordynuje zapis zdjęcia, uruchomienie analizy i obserwowanie szczegółów posiłku.
 */
class MealPhotoWorkflow(
    private val activity: AppCompatActivity,
    private val repository: MealRepository,
    private val detailsRenderer: PhotoDetailsRenderer,
    private val isScanPage: () -> Boolean,
    private val onPhotoOpened: (PhotoHistoryEntity) -> Unit,
    private val onCaptureFinished: () -> Unit
) {
    private var detailsJob: Job? = null

    // Dekoduje bitmapę i uwzględnia orientację zapisaną w metadanych EXIF.
    fun decodeOrientedPhoto(imageBytes: ByteArray): Bitmap? {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
        val rotationDegrees = try {
            ExifInterface(ByteArrayInputStream(imageBytes)).rotationDegrees
        } catch (_: Exception) {
            0
        }
        if (rotationDegrees == 0) return bitmap

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            Matrix().apply { postRotate(rotationDegrees.toFloat()) },
            true
        )
        if (rotatedBitmap !== bitmap) bitmap.recycle()
        return rotatedBitmap
    }

    // Zapisuje oryginał, uruchamia analizę i otwiera wynik po zakończeniu zapisu.
    fun saveCapturedPhoto(imageBytes: ByteArray, note: String?) {
        activity.lifecycleScope.launch {
            try {
                val photo = repository.savePhoto(imageBytes, note)
                AnalysisQueue(activity.applicationContext).enqueuePhoto(imageBytes, photo.id, note)
                android.widget.Toast.makeText(
                    activity,
                    R.string.photo_saved,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                if (isScanPage()) onPhotoOpened(photo)
            } catch (_: Exception) {
                android.widget.Toast.makeText(
                    activity,
                    R.string.photo_save_error,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } finally {
                onCaptureFinished()
            }
        }
    }

    // Subskrybuje stan analizy dla aktualnego zdjęcia i anuluje poprzednią subskrypcję.
    fun renderDetails(
        container: LinearLayout,
        photo: PhotoHistoryEntity,
        onBack: () -> Unit
    ) {
        detailsJob?.cancel()
        val analysisContainer = detailsRenderer.render(container, photo, onBack)
        detailsJob = activity.lifecycleScope.launch {
            repository.observeAnalysisForPhoto(photo.id).collect { details ->
                detailsRenderer.renderAnalysis(photo, details, analysisContainer)
            }
        }
    }

    // Anuluje obserwację, gdy szczegóły zdjęcia przestaną być widoczne.
    fun cancelDetails() {
        detailsJob?.cancel()
        detailsJob = null
    }

    // Ponawia analizę zdjęcia po wcześniejszym błędzie lub dla nowego wpisu historii.
    fun retryPhotoAnalysis(photo: PhotoHistoryEntity) {
        activity.lifecycleScope.launch {
            AnalysisQueue(activity.applicationContext).enqueuePhoto(photo.imageBytes, photo.id, photo.note)
        }
    }
}
