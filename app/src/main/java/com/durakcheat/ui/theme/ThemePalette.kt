package com.durakcheat.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class ThemePalette private constructor(
    colors: List<Color>
) {
    private val colors: List<Color> = if(colors.size < defaultColors.size)
        colors + defaultColors.subList(colors.lastIndex, defaultColors.size)
    else colors

    constructor() : this(defaultColors)

    companion object {
        const val CONTAINER_COLOR_ALPHA: Float = 0.6f
        const val DARK_THEME_DARKNESS: Float = 0.9f
        internal val defaultColors = listOf(
            Color(0xFF00CA00),
            Color(0xFF8DE9A9),
            Color(0xFFFFEE00),
            Color(0xFF000000),
            Color(0xFFFFFFFF),
            Color(0xFFFF4444)
        )
    }

    private val primary  : Color get() = colors[0]
    private val secondary: Color get() = colors[1]
    private val tertiary : Color get() = colors[2]
    private val text     : Color get() = colors[3]
    private val text2    : Color get() = colors[4]
    private val error    : Color get() = colors[5]

    object MoshiAdapter {
        @ToJson
        fun toJson(palette: ThemePalette) = palette.colors.map { it.value.toLong() }

        @FromJson
        fun fromJson(values: List<Long>) = ThemePalette(values.map { Color(it.toULong()) })
    }

    operator fun times(v: Float) = ThemePalette(colors.map { it*v })
    operator fun get(index: Int) = colors[index]

    fun replace(i: Int, color: Color) = ThemePalette(
        colors.toMutableList().apply { this[i] = color }
    )

    fun toColorTheme(isDarkTheme: Boolean)
    = if(isDarkTheme) with(this*DARK_THEME_DARKNESS) {
        darkColorScheme(
            primary = primary,
            onPrimary = text,
            primaryContainer = primary.copy(alpha = CONTAINER_COLOR_ALPHA),

            secondary = secondary,
            onSecondary = text,

            tertiary = tertiary,
            onTertiary = text,

            onSurface = text2,
            onSurfaceVariant = text2,

            error = error,
            errorContainer = error.copy(alpha = CONTAINER_COLOR_ALPHA)
        )
    } else
        lightColorScheme(
            primary = primary,
            onPrimary = text,
            primaryContainer = primary.copy(alpha = CONTAINER_COLOR_ALPHA),

            secondary = secondary,
            onSecondary = text,

            tertiary = tertiary,
            onTertiary = text,

            onSurface = text2,
            onSurfaceVariant = text2,

            error = error,
            errorContainer = error.copy(alpha = CONTAINER_COLOR_ALPHA)
        )
}