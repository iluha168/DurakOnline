package com.durakcheat.ui.component.leaf

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> ListSlider(list: List<T>, value: T, modifier: Modifier = Modifier, onValueChange: (T) -> Unit){
    if(list.isEmpty())
        return LinearProgressIndicator(modifier)
    Slider(
        value = list.indexOf(value).toFloat(),
        onValueChange = {
            onValueChange(list[it.toInt()])
        },
        steps = list.size,
        valueRange = 0f..list.lastIndex.toFloat(),
        modifier = modifier
    )
}