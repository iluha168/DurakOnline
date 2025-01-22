package com.durakcheat.ui.component.leaf

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.durakcheat.ui.component.container.Rov


@Composable
fun TextCounter(n: Int, fontSize: TextUnit = TextUnit.Unspecified, isInText: Boolean = true){
    TextCounter(n.toLong(), fontSize, isInText)
}

@Composable
fun TextCounter(n: Long, fontSize: TextUnit = TextUnit.Unspecified, isInText: Boolean = true){
    AnimatedContent(
        label = "TextCounter",
        targetState = n,
        transitionSpec = {
            val direction = if(targetState > initialState) 1 else -1
            (slideInVertically { height -> height*direction } + fadeIn())
                .togetherWith(slideOutVertically { height -> -height*direction } + fadeOut())
                .using(SizeTransform(clip = false))
        },
        modifier = if(isInText) Modifier.padding(horizontal = 3.dp) else Modifier
    ) {
        Text(
            text = it.toString(),
            fontSize = fontSize,
        )
    }
}

@Composable
fun NamedTextCounterRow(@StringRes strID: Int, n: Int, modifier: Modifier = Modifier){
    Rov(modifier) {
        Text(stringResource(strID) +":")
        TextCounter(n)
    }
}