package com.durakcheat.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.durakcheat.ui.component.container.columnDialog
import com.durakcheat.ui.component.leaf.ButtonTextOnly

@Composable
fun opaqueColorPickerDialog(
    title: String,
    onPick: (Color) -> Unit
): (Color) -> Unit = columnDialog(title = title) {
initialColor, close ->
    var color by remember { mutableStateOf(initialColor) }
    val previewShape = RoundedCornerShape(5.dp)
    Box(modifier = Modifier
        .clip(previewShape)
        .shadow(5.dp, previewShape)
        .padding(5.dp)
        .background(color, previewShape)
        .fillMaxWidth()
        .height(60.dp)
    )
    for(i in listOf<Triple<Float, (Float) -> Color, Color>>(
        Triple(color.red  , { color.copy(red   = it) }, Color.Red  ),
        Triple(color.green, { color.copy(green = it) }, Color.Green),
        Triple(color.blue , { color.copy(blue  = it) }, Color.Blue ),
    )) Slider(
        value = i.first,
        onValueChange = { color = i.second(it) },
        colors = SliderDefaults.colors(thumbColor = i.third, activeTrackColor = i.third),
        modifier = Modifier.fillMaxWidth(),
        valueRange = 0f..1f,
    )
    ButtonTextOnly(text = "Choose", modifier = Modifier.fillMaxWidth(), color = color) {
        onPick(color)
        close()
    }
}