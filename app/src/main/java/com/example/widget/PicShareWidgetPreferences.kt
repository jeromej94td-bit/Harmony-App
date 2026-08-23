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
    private const val KEY_CAPTION = "caption"
    private const val KEY_SHOW_CAPTION = "show_caption"
    private const val KEY_SHOW_STATUS = "show_status"
    private const val KEY_SHUFFLE = "shuffle_pictures"

    fun load(context: Context): PicShareWidgetSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return PicShareWidgetSettings(
            caption = prefs.getString(KEY_CAPTION, "").orEmpty(),
            showCaption = prefs.getBoolean(KEY_SHOW_CAPTION, true),
            showStatus = prefs.getBoolean(KEY_SHOW_STATUS, true),
            shufflePictures = prefs.getBoolean(KEY_SHUFFLE, false)
        )
    }

    fun save(context: Context, settings: PicShareWidgetSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CAPTION, settings.caption)
            .putBoolean(KEY_SHOW_CAPTION, settings.showCaption)
            .putBoolean(KEY_SHOW_STATUS, settings.showStatus)
            .putBoolean(KEY_SHUFFLE, settings.shufflePictures)
            .apply()
    }
}
