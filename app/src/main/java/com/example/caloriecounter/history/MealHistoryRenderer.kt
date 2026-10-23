package com.example.caloriecounter.history

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.example.caloriecounter.data.PhotoHistoryEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Tworzy zwijaną listę historii zdjęć. Nawigację do szczegółów i usuwanie
 * przekazuje aktywności przez callbacki.
 */
class MealHistoryRenderer(
    private val activity: AppCompatActivity,
    private val isDarkTheme: () -> Boolean,
    private val decodePhoto: (ByteArray) -> Bitmap?,
    private val onPhotoClick: (PhotoHistoryEntity) -> Unit,
    private val onDeletePhoto: (Long) -> Unit
) {
    private val expandedDates = mutableSetOf<Long>()

    // Odświeża listę zdjęć, zachowując rozwinięte sekcje dla nadal istniejących dni.
    fun render(container: LinearLayout, photos: List<PhotoHistoryEntity>) {
        container.removeAllViews()
        val days = photos
            .sortedByDescending { it.createdAt }
            .groupBy { dayStart(it.createdAt) }
        expandedDates.retainAll(days.keys)

        if (photos.isEmpty()) {
            container.addView(pageText(activity.getString(R.string.history_empty), 15f, false).apply {
                setPadding(0, 24.dp(), 0, 0)
            })
            return
        }

        days.forEach { (dateStart, dayPhotos) ->
            val expanded = dateStart in expandedDates
            val section = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
            }
            val dateText = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
                .format(Date(dateStart))
            val (header, expandIndicator) = createDateHeader(dateText, dayPhotos.size, expanded)
            val rows = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                visibility = if (expanded) View.VISIBLE else View.GONE
            }

            dayPhotos.forEach { photo -> rows.addView(createPhotoRow(photo)) }

            header.setOnClickListener {
                val nowExpanded = if (dateStart in expandedDates) {
                    expandedDates.remove(dateStart)
                    false
                } else {
                    expandedDates.add(dateStart)
                    true
                }
                rows.visibility = if (nowExpanded) View.VISIBLE else View.GONE
                header.contentDescription = activity.getString(
                    if (nowExpanded) R.string.history_collapse_date else R.string.history_expand_date,
                    dateText
                )
                expandIndicator.text = if (nowExpanded) "-" else "+"
            }
            section.addView(header)
            section.addView(rows)
            container.addView(section, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 12.dp(), 0, 0) })
        }
    }

    // Nagłówek pokazuje datę, liczbę zdjęć i stan rozwinięcia dnia.
    private fun createDateHeader(
        dateText: String,
        photoCount: Int,
        expanded: Boolean
    ): Pair<LinearLayout, TextView> {
        val indicator = pageText(if (expanded) "-" else "+", 20f, true)
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12.dp(), 12.dp(), 12.dp(), 12.dp())
            background = ColorDrawable(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
            isClickable = true
            isFocusable = true
            contentDescription = activity.getString(
                if (expanded) R.string.history_collapse_date else R.string.history_expand_date,
                dateText
            )
            addView(pageText(
                activity.getString(
                    R.string.history_date_header,
                    dateText,
                    activity.resources.getQuantityString(R.plurals.history_meal_count, photoCount, photoCount)
                ),
                15f,
                true
            ), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(indicator)
        }
        return header to indicator
    }

    // Wiersz otwiera szczegóły zdjęcia; przycisk kosza usuwa ten wpis.
    private fun createPhotoRow(photo: PhotoHistoryEntity): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10.dp(), 10.dp(), 8.dp(), 10.dp())
            background = ColorDrawable(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8.dp(), 0, 0) }

            addView(ImageView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(76.dp(), 76.dp())
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(decodePhoto(photo.imageBytes))
                contentDescription = activity.getString(R.string.history_photo_content_description)
            })

            val details = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { setMargins(12.dp(), 0, 4.dp(), 0) }
            }
            details.addView(pageText(
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.createdAt)),
                12f,
                false
            ))
            details.addView(pageText(photo.note ?: activity.getString(R.string.history_photo_default), 15f, true))
            addView(details)
            isClickable = true
            isFocusable = true
            setOnClickListener { onPhotoClick(photo) }

            addView(ImageButton(activity).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                contentDescription = activity.getString(R.string.history_delete)
                background = ColorDrawable(Color.TRANSPARENT)
                setOnClickListener { onDeletePhoto(photo.id) }
            }, LinearLayout.LayoutParams(44.dp(), 44.dp()))
        }

    private fun pageText(text: String, size: Float, bold: Boolean): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = size
            setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun dayStart(timestamp: Long): Long = Calendar.getInstance().run {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
