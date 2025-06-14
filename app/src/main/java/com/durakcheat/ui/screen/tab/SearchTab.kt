package com.durakcheat.ui.screen.tab

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.durakcheat.net.packet.DGameJoin
import com.durakcheat.net.packet.DLookupStartOptions
import com.durakcheat.net.packet.booleans
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.container.columnDialog
import com.durakcheat.ui.component.leaf.ButtonTextOnly
import com.durakcheat.ui.component.leaf.CashDisplay
import com.durakcheat.ui.component.leaf.GameInfoRow
import com.durakcheat.ui.component.leaf.ListSlider
import com.durakcheat.ui.component.leaf.MultiSelectionRow
import com.durakcheat.ui.component.leaf.NamedTextCounterRow
import com.durakcheat.ui.component.leaf.TextCounter
import com.durakcheat.ui.component.leaf.ThickButton

@Composable
fun SearchTab(activity: MainActivity){
    var filter = activity.client.lastLookupFilter.value ?: DLookupStartOptions()
    var betMax by remember { mutableLongStateOf(filter.betMax) }
    var betMin by remember { mutableLongStateOf(filter.betMin) }
    val decks = remember { mutableStateListOf(*filter.deck.toTypedArray()) }
    val players = remember { mutableStateListOf(*filter.players.toTypedArray()) }
    val fast = remember { mutableStateListOf(*filter.fast.toTypedArray()) }
    val cheats = remember { mutableStateListOf(*filter.ch.toTypedArray()) }
    val draw = remember { mutableStateListOf(*filter.dr.toTypedArray()) }
    val neighbors = remember { mutableStateListOf(*filter.nb.toTypedArray()) }
    val passing = remember { mutableStateListOf(*filter.sw.toTypedArray()) }
    val private = remember { mutableStateListOf(*filter.pr.toTypedArray()) }

    LaunchedEffect(Unit) {
        activity.client.lookupStart(filter)
    }
    DisposableEffect(Unit) {
        onDispose {
            activity.client.lookupStop()
        }
    }
    val myBalance = activity.client.balance

    val filterOpener = columnDialog<Unit>(title = stringResource(R.string.a_filter)) { _, closer ->
        var bets by remember { mutableStateOf(emptyList<Long>()) }
        LaunchedEffect(Unit) {
            bets = activity.client.getBets()
        }

        Text(stringResource(R.string.bet))
        Row {
            val col = if(betMin > myBalance) MaterialTheme.colorScheme.error else null
            CashDisplay(betMin, color = col)
            Text(" - ")
            CashDisplay(betMax, color = col)
        }
        ListSlider(list = bets, value = betMin, Modifier.fillMaxWidth()) {
            betMin = it
            if(it > betMax)
                betMax = it
        }
        ListSlider(list = bets, value = betMax, Modifier.fillMaxWidth()) {
            betMax = it
            if(it < betMin)
                betMin = it
        }

        Text(stringResource(R.string.players))
        MultiSelectionRow(
            choices = PLAYERS_INDICES,
            selection = players,
            modifier = Modifier.fillMaxWidth(),
            item = { Text(it.toString()) }
        )

        Text(stringResource(R.string.deck))
        MultiSelectionRow(
            choices = DDeck.entries,
            selection = decks,
            modifier = Modifier.fillMaxWidth(),
            item = { Text(it.toString()) }
        )

        Text(stringResource(R.string.game_rules))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            @Composable
            fun BooleanIconMultiSelectionRow(selection: MutableList<Boolean>, @DrawableRes icoTrue: Int, @DrawableRes icoFalse: Int){
                MultiSelectionRow(choices = booleans, selection = selection) {
                    Icon(painterResource(if(it) icoTrue else icoFalse), null)
                }
            }
            BooleanIconMultiSelectionRow(fast, R.drawable.ico_fast, R.drawable.ico_slow)
            BooleanIconMultiSelectionRow(passing, R.drawable.ico_passing, R.drawable.ico_throw)
            BooleanIconMultiSelectionRow(neighbors, R.drawable.ico_throw_neighbours, R.drawable.ico_throw_all)
            BooleanIconMultiSelectionRow(cheats, R.drawable.ico_tricks, R.drawable.ico_fair)
            BooleanIconMultiSelectionRow(draw, R.drawable.ico_end_draw, R.drawable.ico_end_classic)
            BooleanIconMultiSelectionRow(private, R.drawable.ico_lock, R.drawable.ico_unlock)
        }
        ButtonTextOnly(
            enabled = listOf(decks, players, fast, cheats, draw, neighbors, passing, private).all { it.isNotEmpty() },
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.to_apply)
        ){
            filter = DLookupStartOptions(
                betMax = betMax, betMin = betMin,
                deck = decks, players = players,
                fast = fast,
                ch = cheats, dr = draw, nb = neighbors, sw = passing,
                pr = private,
            )
            closer()
            with(activity.client){
                lookupStop()
                lookupStart(filter)
            }
        }
    }
    // Main content
    TitleText(stringResource(R.string.game_search), Modifier.fillMaxWidth())
    ButtonTextOnly(text = stringResource(R.string.a_filter), modifier = Modifier.fillMaxWidth()){
        filterOpener(Unit)
    }
    LazyListColumn(
        list = activity.client.lookup.values.toList(),
        key = { it.id },
        placeholder = { EmptySpaceFillerText(R.string.no_games_found) }
    ) { game ->
        ThickButton(
            onClick = { activity.client.joinGame(DGameJoin(game.id, null)) },
            modifier = Modifier
                .animateItem()
                .fillMaxWidth(),
            enabled = myBalance >= game.bet && !game.pr,
            slim = true,
        ) {
            Column {
                GameInfoRow(game)
                Row {
                    if (game.pr)
                        Icon(painterResource(R.drawable.ico_lock), stringResource(R.string.password_required))
                    Text(text = game.name, maxLines = 1)
                }
                Rov {
                    NamedTextCounterRow(R.string.players, game.cp)
                    Text("/")
                    TextCounter(game.p)
                }
            }
        }
    }
}