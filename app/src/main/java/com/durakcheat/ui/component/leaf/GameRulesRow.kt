package com.durakcheat.ui.component.leaf

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.durakcheat.R
import com.durakcheat.net.json.DGameBase
import com.durakcheat.ui.component.container.Rov

@Composable
fun GameRulesRow(info: DGameBase) {
    Rov {
        with(info) {
            Icon(painterResource(if(sw) R.drawable.ico_passing else R.drawable.ico_throw), null)
            Icon(painterResource(if(dr == true) R.drawable.ico_end_draw else R.drawable.ico_end_classic), null)
            Icon(painterResource(deck.ico), deck.toString())
            Icon(painterResource(if(nb) R.drawable.ico_throw_neighbours else R.drawable.ico_throw_all), null)
            Icon(painterResource(if(ch) R.drawable.ico_tricks else R.drawable.ico_fair), null)
            Icon(painterResource(if(fast) R.drawable.ico_fast else R.drawable.ico_slow), null)
            CashDisplay(bet, Modifier.weight(1f))
        }
    }
}