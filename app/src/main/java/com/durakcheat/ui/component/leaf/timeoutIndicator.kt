package com.durakcheat.ui.component.leaf

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun timeoutIndicator(durationMs: Long, color: Color): () -> Unit {
    val timeLeft = remember {
        Animatable(1f)
    }
    val coroutineScope = rememberCoroutineScope()
    fun restart() {
        coroutineScope.launch {
            timeLeft.snapTo(1f)
            timeLeft.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = durationMs.toInt(), easing = LinearEasing),
            )
        }
    }
    LaunchedEffect(Unit) {
        restart()
    }
    LinearProgressIndicator(color = color, progress = { timeLeft.value })
    return ::restart
}

@Composable
fun timeoutIndicator(color: Color): () -> Unit {
    LinearProgressIndicator(progress = {1f}, color = color)
    return {}
}