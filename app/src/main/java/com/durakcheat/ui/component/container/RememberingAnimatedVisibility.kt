package com.durakcheat.ui.component.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Displays [content], until [condition] becomes false.
 * Then displays [content] using the last remembered [value] at the time of [condition] == true, for [delay] milliseconds.
 * */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> RememberingAnimatedVisibility(
    value: T,
    condition: (T) -> Boolean,
    onValueConsumed: () -> Unit = {},
    delay: Int = 0,
    content: @Composable (T) -> Unit
){
    var visible by remember { mutableStateOf(false) }
    var rememberedValue by remember { mutableStateOf(value) }
    if(condition(value)) { // New value received
        visible = true
        rememberedValue = value
        onValueConsumed()
    }
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(animationSpec = tween()),
        exit = shrinkHorizontally(animationSpec = tween(delayMillis = delay)),
    ) {
        if(this.transition.currentState == this.transition.targetState)
            visible = false
        content(rememberedValue)
    }
}