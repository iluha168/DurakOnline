package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.ui.thenIf

val CardShape = RectangleShape

@Composable
fun DCardDisplay(card: DCard?, modifier: Modifier = Modifier, trumpSuit: DCardSuit?, small: Boolean = false){
    Text(
        text = card?.toString() ?: "??",
        fontSize = if(small) 20.sp else 28.sp,
        modifier = Modifier
            .padding(if (small) 2.dp else 5.dp)
            .then(modifier)
            .thenIf( card != null && card.suit == trumpSuit,
                Modifier.border(
                    if (small) 0.8.dp else 1.5.dp,
                    MaterialTheme.colorScheme.error, CardShape
                )
            )
            .padding(if (small) 1.dp else 5.dp),
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DCardDisplay(cards: Iterable<DCard>, modifier: Modifier = Modifier, trumpSuit: DCardSuit){
    FlowRow(modifier = modifier) {
        for(card in cards.sorted())
            DCardDisplay(card = card, trumpSuit = trumpSuit, small = true)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DCardDisplay(cards: Int, modifier: Modifier = Modifier){
    FlowRow(modifier = modifier) {
        repeat(cards) {
            DCardDisplay(card = null, trumpSuit = null, small = true)
        }
    }
}