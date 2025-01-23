package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.durakcheat.R
import com.durakcheat.ui.component.container.Rov
import kotlin.math.absoluteValue

internal val qualifiers = listOf("K","M","B","T","Qd","Qn","Sx","Sp","O","N","D","U")

@Composable
fun CashDisplay(n: Long, modifier: Modifier = Modifier, color: Color? = null, fontSize: TextUnit = 20.sp){
    Rov (
        horizontalArrangement = Arrangement.End,
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        var qualifier = ""
        var displayNumber = n
        for(q in qualifiers)
            if(displayNumber.absoluteValue % 1000L == 0L) {
                qualifier = q
                displayNumber /= 1000L
            }
        Text(
            text = "$displayNumber$qualifier",
            fontSize = fontSize,
            color = color ?: Color.Unspecified
        )
        Icon(
            painter = painterResource(R.drawable.ico_cash),
            contentDescription = stringResource(R.string.money),
            modifier = Modifier.fillMaxHeight().aspectRatio(1f),
            tint = color ?: LocalContentColor.current
        )
    }
}

@Composable
fun CoinsDisplay(n: Long, modifier: Modifier = Modifier, fontSize: TextUnit = 20.sp){
    Rov (
        horizontalArrangement = Arrangement.End,
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Text(
            text = n.toString(),
            fontSize = fontSize,
        )
        Icon(
            painter = painterResource(R.drawable.coin),
            contentDescription = stringResource(R.string.coins),
            modifier = Modifier.fillMaxHeight().aspectRatio(1f),
            tint = Color.Unspecified
        )
    }
}