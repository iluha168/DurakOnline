package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durakcheat.ui.component.leaf.TextCounter
import com.durakcheat.ui.component.leaf.ThickButton

@Composable
fun ButtonHand(
    handSize: Int,
    onClick: () -> Unit
) = ThickButton(
    onClick = onClick,
    content = { TextCounter(handSize, 26.sp, isInText = false) },
    color = MaterialTheme.colorScheme.tertiary,
    slim = true, shape = RoundedCornerShape(10.dp),
    modifier = Modifier.width(40.dp)
)


@DPreview
@Composable
private fun PreviewButtonHand() = ButtonHand(Math.random().times(30.0).toInt()) { }