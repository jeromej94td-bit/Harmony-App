package com.example.widget

import android.content.Context

data class PicShareWidgetSettings(
    val caption: String = "",
    val showCaption: Boolean = true,
    val showStatus: Boolean = true,
    val shufflePictures: Boolean = false
)

object PicShareWidgetPreferences {
    private const val PREFS = "picshare_widget_settings"
    private const val CAPTION = "caption"
    private const val SHOW_CAPTION = "show_caption"
    private const val SHOW_STATUS = "show_status"
    private const val SHUFFLE = "shuffle_pictures"

    fun load(context: Context): PicShareWidgetSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return PicShareWidgetSettings(
            caption = prefs.getString(CAPTION, "").orEmpty(),
            showCaption = prefs.getBoolean(SHOW_CAPTION, true),
            showStatus = prefs.getBoolean(SHOW_STATUS, true),
            shufflePictures = prefs.getBoolean(SHUFFLE, false)
        )
    }

    fun save(context: Context, settings: PicShareWidgetSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CAPTION, settings.caption.trim())
            .putBoolean(SHOW_CAPTION, settings.showCaption)
            .putBoolean(SHOW_STATUS, settings.showStatus)
            .putBoolean(SHUFFLE, settings.shufflePictures)
            .apply()
    }
}
