package com.example.caloriecounter.settings

import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Buduje edytor dziennego limitu kalorii i zapisuje poprawną wartość po krótkiej przerwie.
 */
class DailyLimitSettingsController(
    private val activity: AppCompatActivity,
    private val currentLimit: () -> Int,
    private val onLimitSaved: (Int) -> Unit,
    private val isDarkTheme: () -> Boolean
) {
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var inputLayout: TextInputLayout? = null
    private var pendingLimitValue: Int? = null
    private var pendingSave: Runnable? = null

    // Dodaje opis i numeryczne pole edycji do przekazanego ekranu ustawień.
    fun buildPage(container: LinearLayout, limit: Int): TextInputLayout {
        val fieldBackground = if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE
        val fieldText = if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
        val fieldHint = if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#58677A")
        val fieldStroke = if (isDarkTheme()) Color.parseColor("#8291A5") else Color.parseColor("#1E654C")

        val editorLayout = TextInputLayout(activity).apply {
            hint = activity.getString(R.string.daily_limit_input_label)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(fieldBackground)
            boxStrokeColor = fieldStroke
            boxStrokeWidth = 1.dp()
            boxStrokeWidthFocused = 2.dp()
            defaultHintTextColor = android.content.res.ColorStateList.valueOf(fieldHint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dp() }
        }
        inputLayout = editorLayout
        val editor = TextInputEditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            maxLines = 1
            setText(limit.toString())
            setTextColor(fieldText)
            setHintTextColor(fieldHint)
            setPadding(12.dp(), 16.dp(), 12.dp(), 16.dp())
            setSelectAllOnFocus(true)
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) setSelection(text?.length ?: 0) }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(text: Editable?) {
                    scheduleSave(text?.toString())
                }
            })
        }
        editorLayout.addView(editor)

        val valueRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(editorLayout, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(TextView(activity).apply {
                text = activity.getString(R.string.daily_limit_unit)
                textSize = 16f
                setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(12.dp(), 0, 0, 0)
            })
        }
        container.addView(valueRow)
        return editorLayout
    }

    // Zapisuje oczekującą poprawną wartość, gdy użytkownik opuszcza ekran ustawień.
    fun savePending() {
        val newLimit = pendingLimitValue ?: return
        pendingSave?.let(mainThreadHandler::removeCallbacks)
        pendingSave = null
        pendingLimitValue = null
        onLimitSaved(newLimit)
    }

    private fun scheduleSave(rawValue: String?) {
        val newLimit = rawValue?.toIntOrNull()
        if (newLimit == null || newLimit <= 0) {
            inputLayout?.error = activity.getString(R.string.daily_limit_invalid)
            cancelPendingSave()
            return
        }

        inputLayout?.error = null
        if (newLimit == currentLimit()) {
            cancelPendingSave()
            return
        }

        cancelPendingSave()
        pendingLimitValue = newLimit
        val save = Runnable {
            if (pendingLimitValue == newLimit) savePending()
        }
        pendingSave = save
        mainThreadHandler.postDelayed(save, DAILY_LIMIT_SAVE_DELAY_MS)
    }

    private fun cancelPendingSave() {
        pendingSave?.let(mainThreadHandler::removeCallbacks)
        pendingSave = null
        pendingLimitValue = null
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()

    private companion object {
        const val DAILY_LIMIT_SAVE_DELAY_MS = 500L
    }
}
