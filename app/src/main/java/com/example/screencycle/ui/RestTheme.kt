package com.example.screencycle.ui

import androidx.annotation.ColorRes
import com.example.screencycle.R

enum class RestTheme(
    val key: String,
    @ColorRes val backgroundColor: Int,
    @ColorRes val messageColor: Int
) {
    DEFAULT("default", R.color.rest_theme_default_background, R.color.rest_theme_default_text),
    FOREST("forest", R.color.rest_theme_forest_background, R.color.rest_theme_forest_text),
    SUNSET("sunset", R.color.rest_theme_sunset_background, R.color.rest_theme_sunset_text);

    companion object {
        fun fromKey(key: String?): RestTheme =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
