package com.durakcheat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.durakcheat.net.json.StateJSONPreference
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.squareup.moshi.JsonDataException

operator fun Color.times(v: Float) = Color(
    red = red*v,
    green = green*v,
    blue = blue*v,
    alpha = alpha
)

@Composable
fun DurakCheatTheme(
    palettePreference: StateJSONPreference<ThemePalette>,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    try {
        palettePreference.value ?: throw JsonDataException()
    } catch(e: JsonDataException) {
        palettePreference.value = ThemePalette()
    }

    val colorScheme = palettePreference.value!!.toColorTheme(darkTheme)
    val view = LocalView.current
    val sysController = rememberSystemUiController()
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            sysController.setSystemBarsColor(colorScheme.primary)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}