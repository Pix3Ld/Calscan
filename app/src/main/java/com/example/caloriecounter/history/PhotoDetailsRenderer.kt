package com.example.caloriecounter.history

import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.example.caloriecounter.data.JobStatus
import com.example.caloriecounter.data.PhotoAnalysisDetails
import com.example.caloriecounter.data.PhotoHistoryEntity
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Buduje widok zdjęcia oraz prezentuje wynik i status jego analizy.
 */
class PhotoDetailsRenderer(
    private val activity: AppCompatActivity,
    private val isDarkTheme: () -> Boolean,
    private val decodePhoto: (ByteArray) -> Bitmap?,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val onRetryAnalysis: (PhotoHistoryEntity) -> Unit
) {
    // Tworzy układ szczegółów i zwraca kontener, który będzie odświeżany danymi analizy.
    fun render(
        pageContainer: LinearLayout,
        photo: PhotoHistoryEntity,
        onBack: () -> Unit
    ): LinearLayout {
        pageContainer.addView(styledButton(activity.getString(R.string.details_back), false).apply {
            setOnClickListener { onBack() }
        })

        val scrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8.dp(), 0, 16.dp())
        }
        content.addView(ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300.dp()
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setImageBitmap(decodePhoto(photo.imageBytes))
            contentDescription = activity.getString(R.string.history_photo_content_description)
        })
        content.addView(pageText(
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.createdAt)),
            13f,
            false
        ).apply { setPadding(0, 12.dp(), 0, 0) })
        photo.note?.let { note ->
            content.addView(pageText(note, 16f, true).apply { setPadding(0, 8.dp(), 0, 0) })
        }

        val analysisContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16.dp(), 0, 0)
        }
        content.addView(analysisContainer)
        scrollView.addView(content)
        pageContainer.addView(scrollView)
        return analysisContainer
    }

    // Renderuje gotowy wynik, postęp, błąd lub opcję ponownego uruchomienia analizy.
    fun renderAnalysis(
        photo: PhotoHistoryEntity,
        details: PhotoAnalysisDetails,
        container: LinearLayout
    ) {
        container.removeAllViews()
        val summary = details.summary
        val job = details.job

        if (summary != null) {
            container.addView(pageText(summary.title, 22f, true))
            container.addView(pageText("${summary.caloriesKcal} kcal", 28f, true).apply {
                setPadding(0, 4.dp(), 0, 12.dp())
            })
            container.addView(pageText(
                activity.getString(R.string.details_protein_template, summary.proteinGrams),
                16f,
                false
            ).withVerticalPadding())
            container.addView(pageText(
                activity.getString(R.string.details_carbs_template, summary.carbohydratesGrams),
                16f,
                false
            ).withVerticalPadding())
            container.addView(pageText(
                activity.getString(R.string.details_fat_template, summary.fatGrams),
                16f,
                false
            ).withVerticalPadding())
            container.addView(pageText(
                activity.getString(R.string.details_fiber_template, summary.fiberGrams),
                16f,
                false
            ).withVerticalPadding())

            val vitaminsList = try {
                val array = JSONArray(summary.vitaminsJson)
                (0 until array.length()).map { array.getString(it) }.joinToString(", ")
            } catch (_: Exception) {
                ""
            }
            if (vitaminsList.isNotEmpty()) {
                container.addView(pageText(
                    activity.getString(R.string.details_vitamins_template, vitaminsList),
                    16f,
                    false
                ).withVerticalPadding())
            }
        } else if (job != null) {
            when (job.status) {
                JobStatus.QUEUED, JobStatus.PROCESSING -> {
                    container.addView(pageText(activity.getString(R.string.meal_analysis_processing), 18f, true).apply {
                        setPadding(0, 12.dp(), 0, 8.dp())
                    })
                }
                JobStatus.FAILED -> {
                    val isModelMissing = job.errorMessage?.contains("Gemma model is not installed") == true
                    val message = if (isModelMissing) {
                        activity.getString(R.string.meal_analysis_model_missing)
                    } else {
                        val detailsMsg = job.errorMessage?.takeIf { it.isNotBlank() } ?: ""
                        if (detailsMsg.isNotEmpty()) {
                            "${activity.getString(R.string.meal_analysis_failed)} ($detailsMsg)"
                        } else {
                            activity.getString(R.string.meal_analysis_failed)
                        }
                    }
                    container.addView(pageText(message, 16f, false).apply {
                        setTextColor(Color.parseColor("#E53935"))
                        setPadding(0, 12.dp(), 0, 12.dp())
                    })
                    container.addView(retryButton(R.string.meal_analysis_retry, photo))
                }
                else -> Unit
            }
        } else {
            container.addView(pageText(activity.getString(R.string.meal_analysis_no_analysis), 15f, false).apply {
                setPadding(0, 12.dp(), 0, 12.dp())
            })
            container.addView(retryButton(R.string.meal_analysis_start, photo))
        }
    }

    private fun retryButton(labelRes: Int, photo: PhotoHistoryEntity): MaterialButton =
        styledButton(activity.getString(labelRes), true).apply {
            setOnClickListener { onRetryAnalysis(photo) }
        }

    private fun pageText(text: String, size: Float, bold: Boolean): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = size
            setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun TextView.withVerticalPadding(): TextView = apply {
        setPadding(0, 6.dp(), 0, 6.dp())
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
