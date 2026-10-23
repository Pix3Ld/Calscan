package com.example.caloriecounter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.data.PhotoHistoryEntity

/**
 * Utrzymuje bieżącą stronę i wykonuje wspólne przejścia między ekranami aplikacji.
 */
class MainPageNavigator(
    private val activity: AppCompatActivity,
    private val title: TextView,
    private val pageContainer: LinearLayout,
    private val homeContentViews: List<View>,
    private val beforePageChange: () -> Unit,
    private val onHomeShown: () -> Unit,
    private val renderPage: (Page) -> Unit,
    private val setDetailsBackEnabled: (Boolean) -> Unit
) {
    var currentPage: Page = Page.HOME
        private set
    var detailsReturnPage: Page = Page.HISTORY
        private set
    private var currentPhoto: PhotoHistoryEntity? = null

    // Zmienia ekran i wykonuje wspólne porządki przed zbudowaniem nowej zawartości.
    fun showPage(page: Page): Boolean {
        beforePageChange()
        setDetailsBackEnabled(false)
        currentPage = page

        if (page == Page.HOME) {
            pageContainer.visibility = View.GONE
            homeContentViews.forEach { it.visibility = View.VISIBLE }
            title.text = activity.getString(R.string.title_dashboard)
            onHomeShown()
            return true
        }

        homeContentViews.forEach { it.visibility = View.GONE }
        pageContainer.visibility = View.VISIBLE
        pageContainer.removeAllViews()
        title.text = when (page) {
            Page.SCAN -> activity.getString(R.string.title_scan)
            Page.HISTORY -> activity.getString(R.string.title_history)
            Page.SETTINGS -> activity.getString(R.string.title_settings)
            Page.REMINDER_SETTINGS -> activity.getString(R.string.settings_reminders_title)
            Page.LIMIT_SETTINGS -> activity.getString(R.string.settings_daily_limit)
            Page.DETAILS -> activity.getString(R.string.title_details)
            Page.HOME -> activity.getString(R.string.title_dashboard)
        }
        renderPage(page)
        return true
    }

    fun openSettingsDetail(page: Page) {
        detailsReturnPage = Page.SETTINGS
        showPage(page)
        setDetailsBackEnabled(true)
    }

    // Otwiera szczegóły posiłku i zapamiętuje stronę, do której ma wrócić użytkownik.
    fun showPhotoDetails(photo: PhotoHistoryEntity, returnPage: Page) {
        currentPhoto = photo
        detailsReturnPage = returnPage
        showPage(Page.DETAILS)
        setDetailsBackEnabled(true)
    }

    fun currentPhoto(): PhotoHistoryEntity? = currentPhoto

    fun navigateBackFromDetails() {
        showPage(detailsReturnPage)
    }
}
