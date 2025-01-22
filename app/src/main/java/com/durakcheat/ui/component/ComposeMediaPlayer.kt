package com.durakcheat.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.durakcheat.MainActivity

@Composable
fun rememberRandomizedMediaPlayer(activity: MainActivity, getSounds: () -> Collection<Int>): () -> Unit {
    return remember {
        val sounds = getSounds()
        return@remember {
            activity.playSound(sounds.random())
        }
    }
}