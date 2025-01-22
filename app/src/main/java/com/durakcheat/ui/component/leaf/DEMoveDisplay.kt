package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.durakcheat.R
import com.durakcheat.engine.DEMove
import com.durakcheat.net.json.DCardSuit

@Composable
fun DEMoveDisplay(move: DEMove, trumpSuit: DCardSuit){
    when(move) {
        is DEMove.Place -> {
            Icon(painterResource(R.drawable.ico_throw), "Place")
            DCardDisplay(move.card, trumpSuit = trumpSuit, small = true)
        }
        is DEMove.ThrowIn -> {
            Icon(painterResource(R.drawable.ico_throw), "Throw")
            DCardDisplay(move.card, trumpSuit = trumpSuit, small = true)
        }
        is DEMove.AddTake -> {
            Icon(painterResource(R.drawable.ico_throw), "Add take")
            DCardDisplay(move.card, trumpSuit = trumpSuit, small = true)
        }
        is DEMove.Swap -> {
            Icon(painterResource(R.drawable.ico_passing), "Pass to next")
            DCardDisplay(move.card, trumpSuit = trumpSuit, small = true)
        }
        is DEMove.Beat -> Column(
            Modifier
            .border(1.dp, MaterialTheme.colorScheme.secondary, CardShape)
        ) {
            DCardDisplay(move.board, trumpSuit = trumpSuit, small = true)
            DCardDisplay(move.beatWith, trumpSuit = trumpSuit, small = true)
        }
        else -> Text(move.javaClass.simpleName)
    }
}