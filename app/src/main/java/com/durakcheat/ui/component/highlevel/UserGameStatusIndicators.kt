package com.durakcheat.ui.component.highlevel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durakcheat.R
import com.durakcheat.ui.component.container.RememberingAnimatedVisibility
import com.durakcheat.ui.component.leaf.CashDisplay
import com.durakcheat.ui.component.leaf.ThickButton

@Composable
fun UserGameStatusIndicators(
    isReady: Boolean,
    hasGameStarted: Boolean,
    hasDisconnected: Boolean,

    wantsToSwap: Boolean,
    onClickSwap: () -> Unit,

    winAmount: Long,
    isWinAmountShown: (amount: Long) -> Boolean
) {
    Column(
        modifier = Modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(isReady)
            Icon(Icons.Default.Check, stringResource(R.string.is_ready))
        if(!hasGameStarted)
            ThickButton(
                onClick = onClickSwap,
                slim = true, shape = CircleShape,
                color = if(wantsToSwap) MaterialTheme.colorScheme.primary else null,
                content = { Icon(painterResource(R.drawable.swap), stringResource(R.string.to_swap)) },
                modifier = Modifier.width(40.dp)
            )
        if(hasDisconnected)
            Icon(Icons.Default.Build, stringResource(R.string.disconnected))
        // Cash won/lost
        RememberingAnimatedVisibility(
            value = winAmount,
            condition = isWinAmountShown,
            content = { CashDisplay(it, fontSize = 16.sp) },
            delay = 1000
        )
    }
}

@DPreview
@Composable
private fun PreviewUserGameStatusIndicators() = Row(Modifier.background(Color.Gray)) {
    UserGameStatusIndicators(
        isReady = true,
        hasGameStarted = true,
        hasDisconnected = true,

        wantsToSwap = true,
        onClickSwap = {},

        winAmount = 100L,
        isWinAmountShown = { true }
    )
    VerticalDivider()
    UserGameStatusIndicators(
        isReady = true,
        hasGameStarted = false,
        hasDisconnected = false,

        wantsToSwap = true,
        onClickSwap = {},

        winAmount = 50L,
        isWinAmountShown = { true }
    )
}