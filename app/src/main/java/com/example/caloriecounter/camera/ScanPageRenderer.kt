package com.example.caloriecounter.camera

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Buduje elementy interfejsu ekranu skanowania i podłącza do nich kontroler aparatu.
 */
class ScanPageRenderer(
    private val activity: AppCompatActivity,
    private val cameraController: MealCameraController,
    private val pageText: (String, Float, Boolean) -> android.widget.TextView,
    private val styledButton: (String, Boolean) -> MaterialButton
) {
    // Tworzy podgląd, akcję zdjęcia i pole notatki; zwraca pole do aktualizacji motywu.
    fun render(container: LinearLayout): TextInputLayout {
        container.addView(pageText(activity.getString(R.string.scan_page_intro), 16f, true))

        val cameraFrame = object : FrameLayout(activity) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val width = MeasureSpec.getSize(widthMeasureSpec)
                val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
                var measuredWidth = width
                var measuredHeight = measuredWidth * 9 / 8

                if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED &&
                    measuredHeight > maxHeight
                ) {
                    measuredHeight = maxHeight
                    measuredWidth = measuredHeight * 8 / 9
                }

                setMeasuredDimension(measuredWidth, measuredHeight)
                measureChildren(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                )
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16.dp(), 0, 16.dp()) }
            setBackgroundColor(Color.BLACK)
        }

        val cameraTexture = TextureView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isOpaque = true
        }
        cameraFrame.addView(cameraTexture)

        // Przycisk wznowienia podglądu pokazuje się po wyłączeniu aparatu.
        val cameraPlaceholder = styledButton(
            activity.getString(R.string.camera_enable_button),
            false
        ).apply {
            visibility = View.GONE
            setOnClickListener { cameraController.enablePreview() }
        }
        cameraFrame.addView(
            cameraPlaceholder,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        container.addView(cameraFrame)

        // Główna akcja zdjęcia pozostaje nieaktywna, dopóki aparat nie będzie gotowy.
        val scanButton = styledButton(activity.getString(R.string.scan_button), true).apply {
            textSize = 18f
            minHeight = 64.dp()
            isEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                64.dp()
            ).apply { setMargins(0, 8.dp(), 0, 12.dp()) }
            setOnClickListener { cameraController.capturePhoto() }
        }
        container.addView(scanButton)

        // Notatka jest przekazywana kontrolerowi aparatu i później zapisywana razem ze zdjęciem.
        val noteInput = TextInputEditText(activity).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 1
            maxLines = 2
            setPadding(0, 8.dp(), 0, 8.dp())
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) cameraController.disableForNote()
            }
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!text.isNullOrEmpty()) cameraController.disableForNote()
                }

                override fun afterTextChanged(editable: android.text.Editable?) = Unit
            })
        }
        val detailsInput = TextInputLayout(activity).apply {
            hint = activity.getString(R.string.scan_details_hint)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(noteInput)
        }
        container.addView(detailsInput)
        cameraController.attach(cameraTexture, cameraPlaceholder, scanButton, noteInput)
        return detailsInput
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
