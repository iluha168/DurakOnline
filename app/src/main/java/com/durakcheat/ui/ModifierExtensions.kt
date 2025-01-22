package com.durakcheat.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

fun Modifier.thenIf(condition: Boolean, other: Modifier): Modifier =
    if (condition) then(other) else this

fun Modifier.thenIfElse(condition: Boolean, ifTrue: Modifier, ifFalse: Modifier): Modifier =
    this then if(condition) ifTrue else ifFalse

fun Modifier.stackableBorder(isVisible: Boolean, color: Color, shape: Shape): Modifier =
    this.thenIf(isVisible, Modifier.border(2.dp, color, shape))
        .padding(all = 2.dp)

fun Modifier.shadow(elevation: Dp, color: Color, shape: Shape = RectangleShape) =
    this.shadow(elevation = elevation, spotColor = color, ambientColor = Color.Transparent, shape = shape)

fun Modifier.noClip() = this.graphicsLayer { clip = false }

fun Modifier.animatePlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animation by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    this
        .onPlaced { targetOffset = it.positionInParent().round() }
        .offset {
            val anim = animation ?: Animatable(targetOffset, IntOffset.VectorConverter)
                .also { animation = it }
            if (anim.targetValue != targetOffset)
                scope.launch { anim.animateTo(targetOffset) }
            // Offset the child in the opposite direction to the targetOffset, and slowly catch
            // up to zero offset via an animation to achieve an overall animated movement.
            animation?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}