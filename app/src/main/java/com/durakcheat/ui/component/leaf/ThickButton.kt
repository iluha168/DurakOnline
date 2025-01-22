package com.durakcheat.ui.component.leaf

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.durakcheat.ui.shadow
import com.durakcheat.ui.theme.times
import com.durakcheat.ui.thenIf

/** Prevent 2 thick buttons to animate at the same time */
internal var WAS_PRESS_EVENT_CONSUMED = false

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun ThickButton(
    modifier: Modifier = Modifier,
    color: Color? = null,
    enabled: Boolean = true,
    slim: Boolean = false,
    shape: Shape = if(slim) RectangleShape else CircleShape,
    maxPressDistance: Dp = 4.dp,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressOffset by animateDpAsState(
        targetValue = if(isPressed || !enabled) 0.dp else maxPressDistance,
        label = "Press animation",
        animationSpec = spring(stiffness = Spring.StiffnessHigh)
    )
    val enabledPadding by animateDpAsState(
        targetValue = if(enabled) maxPressDistance else 0.dp,
        label = "Enabled animation",
    )

    Box(modifier = modifier
        .padding(2.dp)
        .width(IntrinsicSize.Min)
        .height(IntrinsicSize.Min)
    ) {
        val col = color ?: if(slim) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
        val colDisabled = col*0.7f
        val paddingValues = if(slim) PaddingValues(0.dp) else ButtonDefaults.ContentPadding
        Button(
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = colDisabled
            ),
            shape = shape,
            enabled = false,
            modifier = Modifier
                .padding(top = enabledPadding)
                .shadow(pressOffset*5, col, shape)
                .fillMaxSize(),
            contentPadding = paddingValues,
            onClick = {}, content = {}
        )
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = col,
                disabledContainerColor = colDisabled,
                contentColor = contentColorFor(col),
                disabledContentColor = contentColorFor(colDisabled)
            ),
            shape = shape,
            enabled = enabled,
            modifier = Modifier
                .padding(top = enabledPadding)
                .thenIf(enabled, Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            if (isPressed) {
                                isPressed = false
                                WAS_PRESS_EVENT_CONSUMED = false
                            }
                            val event = awaitPointerEvent()
                            if (!WAS_PRESS_EVENT_CONSUMED && event.type == PointerEventType.Press) {
                                isPressed = true
                                WAS_PRESS_EVENT_CONSUMED = true
                            }
                        }
                    }
                )
                .offset(y = -pressOffset)
                .fillMaxSize(),
            contentPadding = paddingValues,
            content = content
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if(isPressed)
                WAS_PRESS_EVENT_CONSUMED = false
        }
    }
}