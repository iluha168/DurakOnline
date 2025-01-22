package com.durakcheat.ui.screen.tab

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.PLAYERS_INDICES
import com.durakcheat.net.json.PLAYERS_MIN
import com.durakcheat.net.packet.DGameCreation
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.leaf.BooleanSelectionRow
import com.durakcheat.ui.component.leaf.CashDisplay
import com.durakcheat.ui.component.leaf.ListSlider
import com.durakcheat.ui.component.leaf.PlayButton
import com.durakcheat.ui.component.leaf.SelectionRow

@Composable
fun IconBooleanSelection(@DrawableRes ico0: Int, @DrawableRes ico1: Int, b: MutableState<Boolean>){
    BooleanSelectionRow(b) {
        Icon(painterResource(if(it) ico0 else ico1), null)
    }
}

@Composable
fun ColumnScope.CreateTab(activity: MainActivity){
    var bets by remember { mutableStateOf(emptyList<Long>()) }
    LaunchedEffect(Unit) {
        bets = activity.client.getBets()
    }
    val defaults = activity.client.lastCreatedGame.value
        ?: DGameCreation(100L, null, PLAYERS_MIN, DDeck.entries.first(), sw=true, ch=false, dr=false, nb=true, fast=true)
    var bet by remember { mutableLongStateOf(defaults.bet) }
    var playersCount by remember { mutableIntStateOf(defaults.players) }
    var deck by remember { mutableStateOf(defaults.deck) }
    var password by remember { mutableStateOf(defaults.password ?: "") }

    val isFast = remember { mutableStateOf(defaults.fast) }
    val hasPassing = remember { mutableStateOf(defaults.sw) }
    val isNeighboursOnly = remember { mutableStateOf(defaults.nb) }
    val hasTricks = remember { mutableStateOf(defaults.ch) }
    val hasDraw = remember { mutableStateOf(defaults.dr == true) }
    val isEnoughBalance = activity.client.balance >= bet

    TitleText("Create a game", Modifier.fillMaxWidth())
    Text(stringResource(R.string.bet))
    CashDisplay(bet, color = if (isEnoughBalance) null else MaterialTheme.colorScheme.error)
    ListSlider(list = bets, value = bet, modifier = Modifier.fillMaxWidth()) { bet = it }

    Text(stringResource(R.string.players))
    SelectionRow(values = PLAYERS_INDICES, value = playersCount, {playersCount = it}) {
        Text(text = it.toString())
    }

    Text(stringResource(R.string.deck))
    SelectionRow(values = DDeck.entries, value = deck, onSelect = {deck = it}) {
        Icon(painterResource(it.ico), it.toString())
    }

    Text(stringResource(R.string.game_rules))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconBooleanSelection(ico0 = R.drawable.ico_fast, ico1 = R.drawable.ico_slow, b = isFast)
        IconBooleanSelection(ico0 = R.drawable.ico_passing, ico1 = R.drawable.ico_throw, b = hasPassing)
        IconBooleanSelection(ico0 = R.drawable.ico_throw_neighbours, ico1 = R.drawable.ico_throw_all, b = isNeighboursOnly)
        IconBooleanSelection(ico0 = R.drawable.ico_tricks, ico1 = R.drawable.ico_fair, b = hasTricks)
        IconBooleanSelection(ico0 = R.drawable.ico_end_draw, ico1 = R.drawable.ico_end_classic, b = hasDraw)
    }

    Text("Password")
    // Rov inside a column because we do not want the password field to take up space when unnecessary
    Column(Modifier.weight(1f)) {
        Rov {
            Icon(painterResource(R.drawable.ico_lock), "Lock")
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    PlayButton(
        text = "Create game",
        modifier = Modifier.fillMaxWidth(),
        enabled = (isNeighboursOnly.value == (playersCount <= 3)) && (playersCount*6 <= deck.size) && isEnoughBalance
    ) {
        activity.client.gameCreate(DGameCreation(
            bet = bet,
            players = playersCount,
            deck = deck,
            ch = hasTricks.value,
            sw = hasPassing.value,
            fast = isFast.value,
            nb = isNeighboursOnly.value,
            dr = hasDraw.value,
            password = password.ifEmpty { null },
        ))
    }
}