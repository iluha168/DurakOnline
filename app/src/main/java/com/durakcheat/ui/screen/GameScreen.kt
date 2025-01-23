package com.durakcheat.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DPlayerMode
import com.durakcheat.net.json.DSmile
import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.ui.animatePlacement
import com.durakcheat.ui.component.Watcher
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.RememberingAnimatedVisibility
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.columnDialog
import com.durakcheat.ui.component.container.lazyColumnDialog
import com.durakcheat.ui.component.highlevel.ButtonHand
import com.durakcheat.ui.component.highlevel.ButtonHandShare
import com.durakcheat.ui.component.highlevel.ButtonSmile
import com.durakcheat.ui.component.highlevel.UserGameStatusIndicators
import com.durakcheat.ui.component.highlevel.playerCardsBreakdown
import com.durakcheat.ui.component.leaf.ButtonTextOnly
import com.durakcheat.ui.component.leaf.CardShape
import com.durakcheat.ui.component.leaf.DCardDisplay
import com.durakcheat.ui.component.leaf.GameRulesRow
import com.durakcheat.ui.component.leaf.NamedTextCounterRow
import com.durakcheat.ui.component.leaf.TextCounter
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.component.leaf.UserAvatar
import com.durakcheat.ui.component.leaf.timeoutIndicator
import com.durakcheat.ui.component.rememberRandomizedMediaPlayer
import com.durakcheat.ui.dialog.confirmationDialog
import com.durakcheat.ui.stackableBorder
import com.durakcheat.ui.thenIf

val avatarSize = 70.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(activity: MainActivity){
    val game = activity.client.game ?: return
    val trumpSuit = game.pos.trump.suit
    val canThrowInAny = game.canThrowInAny()
    val unseenCardsCache = game.unknownCardCandidates

    var handCardSelected by remember { mutableStateOf<DCard?>(null) }
    val invitedFriendIDs = remember { mutableStateListOf<Long>() }

    val soundCardPlace = rememberRandomizedMediaPlayer(activity) {
        listOf(R.raw.card_place_1, R.raw.card_place_2, R.raw.card_place_3)
    }
    val soundCardDiscard = rememberRandomizedMediaPlayer(activity) {
        listOf(R.raw.board_discard_1, R.raw.board_discard_2, R.raw.board_discard_3)
    }

    val surrenderDlgOpener = confirmationDialog<Unit>(
        confirmBtnText = R.string.to_surrender,
        titleText = R.string.to_surrender,
        bodyText = stringResource(R.string.dlg_text_surrender),
        onConfirm = { game.surrender() }
    )
    BackHandler {
        if (game.started && game.clientPlayer.mode != DPlayerMode.WIN)
            surrenderDlgOpener(Unit)
        else {
            game.leave()
            activity.nav.navigateUp()
        }
    }

    val cardsOfPlayerDlgOpener = lazyColumnDialog<Int>(title = stringResource(R.string.cards_of_player)) {
        i -> playerCardsBreakdown(game.pos.players[i].cards, trumpSuit)
    }

    val inviteFriendsDlgOpener = columnDialog(title = stringResource(R.string.dlg_title_friend_invite)) {
        val acceptedFriends = game.client.friends.entries.filter { it.value.raw.kind == DFriendListEntryType.FRIEND }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(90.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items(acceptedFriends) {
                ThickButton(
                    slim = true,
                    enabled = it.key !in invitedFriendIDs,
                    onClick = {
                        game.invite(it.value.raw)
                        invitedFriendIDs.add(it.key)
                    },
                    content = { UserAvatar(it.value.raw.user, null, 80.dp) }
                )
            }
        }
    }

    val emojiDlgOpener = columnDialog(title = stringResource(R.string.smile)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DSmile.vanillaSmiles){
                ButtonSmile(it) { game.smile(it) }
            }
        }
    }

    val discardedCardsDlgOpener = columnDialog(title = stringResource(R.string.cards_discarded)) {
        val unknownAmount = game.pos.deckDiscardedAmount - game.pos.deckDiscarded.size
        if(unknownAmount <= 0)
            Text(stringResource(R.string.cards_discarded_known_all))
        else Row {
            Text(stringResource(R.string.warning))
            TextCounter(unknownAmount)
            Text(stringResource(R.string.cards_discarded_known_not).lowercase())
        }
        HorizontalDivider()
        DCardDisplay(cards = game.pos.deckDiscarded, trumpSuit = trumpSuit)
    }

    val unseenCardsDlgOpener = columnDialog(title = stringResource(R.string.cards_unseen)) {
        DCardDisplay(cards = unseenCardsCache, trumpSuit = trumpSuit)
    }

    val openRoomDlgOpener = confirmationDialog<Unit>(
        titleText = R.string.unlock,
        bodyText = stringResource(R.string.dlg_body_room_unlock, game.pos.info.password ?: ""),
        onConfirm = { game.publish() }
    )

    Column(Modifier.fillMaxWidth()) {
        GameRulesRow(game.pos.info)
        Row(modifier = Modifier.weight(1f)) {
            // User avatars column
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                game.pos.players.forEachIndexed { playerIndex, player ->
                    val playerSocial = game.players[playerIndex]
                    Rov(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .padding(1.dp)
                                .thenIf(playerIndex == game.pos.posAttacker, Modifier.border(1.dp, MaterialTheme.colorScheme.primary))
                                .thenIf(playerIndex == game.pos.posDefender, Modifier.border(1.dp, MaterialTheme.colorScheme.error))
                                .padding(1.dp)
                        ) {
                            Column(Modifier.width(avatarSize)) {
                                UserAvatar(playerSocial.user, activity.nav, avatarSize,
                                    Modifier.alpha(if(game.started && game.pos.deckLeft == 0 && player.cards.isEmpty()) 0.4f else 1f)
                                )
                                val timeoutRestarter =
                                    if (game.started || !game.btnReadyOn)
                                        when (player.mode) {
                                            DPlayerMode.BEAT -> timeoutIndicator(game.pos.info.timeout, MaterialTheme.colorScheme.error)
                                            DPlayerMode.BEAT_DONE -> timeoutIndicator(MaterialTheme.colorScheme.error)
                                            DPlayerMode.PLACE, DPlayerMode.THROW_IN_TAKE -> timeoutIndicator(game.pos.info.timeout, MaterialTheme.colorScheme.primary)
                                            DPlayerMode.THROW_IN -> timeoutIndicator(MaterialTheme.colorScheme.primary)
                                            DPlayerMode.IDLE, DPlayerMode.DONE, DPlayerMode.PASS, DPlayerMode.WIN -> timeoutIndicator(Color.Unspecified)
                                            DPlayerMode.TAKE, DPlayerMode.CONFIRM -> timeoutIndicator(MaterialTheme.colorScheme.tertiary)
                                        }
                                    else if (playerSocial.ready) timeoutIndicator(MaterialTheme.colorScheme.primary)
                                    else timeoutIndicator(game.acceptGameTimeout, MaterialTheme.colorScheme.primary)

                                Watcher({ player.mode }, { mode ->
                                    if(mode == DPlayerMode.TAKE) activity.playSound(R.raw.pop)
                                    timeoutRestarter()
                                    handCardSelected = null
                                })
                                Watcher({ playerSocial.disconnected }, timeoutRestarter)
                                Watcher({ player.cards.size }, timeoutRestarter)
                            }
                            RememberingAnimatedVisibility(
                                value = playerSocial.smile,
                                condition = { it != null },
                                onValueConsumed = { playerSocial.smile = null },
                                delay = 2000
                            ) {
                                Icon(
                                    painterResource(it!!.img), it.name,
                                    modifier = Modifier.size(avatarSize),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                        Column {
                            // Amount of cards
                            if(game.started && game.players[playerIndex].winAmount == 0L)
                                ButtonHand(player.cards.size) {
                                    cardsOfPlayerDlgOpener(playerIndex)
                                }
                            // Share hand with a friend
                            val friend = activity.client.friends[playerSocial.user?.id]?.raw
                            if(game.started && friend?.kind == DFriendListEntryType.FRIEND) {
                                var lastSharedHand by remember { mutableIntStateOf(0) }
                                val handHash = game.pos.hand.toTypedArray().contentHashCode()
                                ButtonHandShare(lastSharedHand != handHash) {
                                    game.friendShareHand(friend)
                                    lastSharedHand = handHash
                                }
                            }
                        }
                        UserGameStatusIndicators(
                            isReady = playerSocial.ready,
                            hasGameStarted = game.started,
                            hasDisconnected = playerSocial.disconnected,
                            wantsToSwap = playerSocial.wantsToSwap,
                            onClickSwap = { game.swap(playerIndex) },
                            winAmount = game.players[playerIndex].winAmount,
                            isWinAmountShown = { game.started && it != 0L }
                        )
                    }
                }
                if(!game.started)
                    Icon(Icons.Default.Add, stringResource(R.string.dlg_title_friend_invite), modifier = Modifier
                        .clickable(onClick = inviteFriendsDlgOpener)
                        .size(avatarSize)
                    )
                Watcher({ game.players[game.myPosition].winAmount }, { win ->
                    if(win > 0) activity.playSound(R.raw.win)
                    if(win < 0) {
                        activity.playSound(R.raw.loss)
                    }
                })
            }
            // Gameplay column
            Column(Modifier.fillMaxSize()) {
                Rov(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    NamedTextCounterRow(R.string.deck_left, game.pos.deckLeft)
                    DCardDisplay(game.pos.trump, trumpSuit = trumpSuit)
                }
                ThickButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = discardedCardsDlgOpener,
                    color = MaterialTheme.colorScheme.secondary
                ){
                    Text(stringResource(R.string.to_check))
                    TextCounter(game.pos.deckDiscardedAmount)
                    Text(stringResource(R.string.cards_discarded).lowercase())
                }
                ThickButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = unseenCardsDlgOpener,
                    color = MaterialTheme.colorScheme.secondary
                ){
                    Text(stringResource(R.string.to_check))
                    TextCounter(unseenCardsCache.size)
                    Text(stringResource(R.string.cards_unseen).lowercase())
                }

                // Board
                Watcher({ game.pos.board.size }, { sNew, sOld ->
                    if(sNew > sOld) soundCardPlace()
                    else if(sNew < sOld) soundCardDiscard()
                })
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for(stack in game.pos.board) {
                        Watcher({ stack.second }, { new ->
                            if(new != null) soundCardPlace()
                        })
                        val canBeat = stack.second == null && handCardSelected?.beats(stack.first, trumpSuit) == true
                        Column(
                            modifier = Modifier
                                .animatePlacement()
                                .animateContentSize()
                                .padding(4.dp)
                                .border(3.dp, MaterialTheme.colorScheme.secondary, CardShape),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DCardDisplay(
                                card = stack.first,
                                modifier = Modifier
                                    .stackableBorder(canBeat, MaterialTheme.colorScheme.primary, CardShape)
                                    .clickable {
                                        if (canBeat) game.beat(stack.first, handCardSelected!!)
                                        handCardSelected = null
                                    },
                                trumpSuit = trumpSuit
                            )
                            if(stack.second != null)
                                DCardDisplay(card = stack.second, trumpSuit = trumpSuit)
                        }
                    }
                    if(game.clientPlayer.mode == DPlayerMode.BEAT)
                        for(card in game.pos.hand.filterNotNull())
                            if(game.canSkipAround(card))
                                ThickButton(onClick = { game.passCard(card) }) {
                                    Icon(Icons.Default.Refresh, stringResource(R.string.move_pass_to_next))
                                    DCardDisplay(card, trumpSuit = trumpSuit, small = true)
                                }
                    // Placeholder
                    if(game.pos.board.isEmpty())
                        EmptySpaceFillerText(R.string.empty_board)
                }
                // Tips
                if(game.started) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Place suggestion
                        val next = game.nextPlayer
                        if (game.clientPlayer.mode == DPlayerMode.PLACE && game.pos.board.isEmpty() && next != null) {
                            val suggestion = game.pos.hand
                                .filter { it?.suit != trumpSuit }
                                .ifEmpty { game.pos.hand }
                                .filterNotNull()
                                .run {
                                    minOfOrNull { it.value }?.let { minVal ->
                                        filter { it.value == minVal }
                                    }
                                }
                                ?.run {
                                    next.cards.size.let {
                                        if (it < size) subList(0, it) else this
                                    }
                                }
                            if (suggestion != null)
                                ThickButton(
                                    onClick = { for (card in suggestion) game.playCard(card) },
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.to_place))
                                    DCardDisplay(cards = suggestion, trumpSuit = trumpSuit)
                                }
                        }
                    }
                }
            }
        }
        // My cards
        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            for(card in game.pos.hand.filterNotNull().sorted()){
                val isCardUseful: Boolean = when(game.clientPlayer.mode) {
                    DPlayerMode.THROW_IN_TAKE, DPlayerMode.PASS, DPlayerMode.PLACE, DPlayerMode.THROW_IN, DPlayerMode.DONE ->
                        canThrowInAny && game.canThrowIn(card)
                    DPlayerMode.BEAT ->
                        game.pos.board.any { it.second == null && card.beats(it.first, trumpSuit) }
                    else -> false
                }
                DCardDisplay(
                    card = card,
                    modifier = Modifier
                        .animatePlacement()
                        .stackableBorder(card == handCardSelected, MaterialTheme.colorScheme.primary, CardShape)
                        .stackableBorder(isCardUseful, MaterialTheme.colorScheme.tertiary, CardShape)
                        .clickable {
                            when (game.clientPlayer.mode) {
                                DPlayerMode.THROW_IN_TAKE, DPlayerMode.PASS, DPlayerMode.PLACE, DPlayerMode.THROW_IN, DPlayerMode.DONE ->
                                    if (isCardUseful) game.playCard(card)

                                DPlayerMode.BEAT ->
                                    handCardSelected = card

                                else ->
                                    handCardSelected = null
                            }
                        },
                    trumpSuit = trumpSuit
                )
            }
        }
        // Bottom action bar
        Rov(Modifier.height(IntrinsicSize.Min)) {
            FlowRow(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val actionBtn =
                    if(game.btnReadyOn && !game.players[game.myPosition].ready) {
                        R.string.is_ready to { game.ready() }
                    }
                    else when(game.clientPlayer.mode){
                        DPlayerMode.THROW_IN_TAKE ->
                            R.string.move_pass to { game.pass() }
                        DPlayerMode.PLACE ->
                            if(game.pos.board.isEmpty()) null
                            else R.string.move_done to { game.done() }
                        DPlayerMode.CONFIRM ->
                            R.string.move_confirm to { game.confirmTake() }
                        DPlayerMode.BEAT ->
                            R.string.move_take to { game.take() }
                        else -> null
                }
                if(actionBtn != null)
                    ButtonTextOnly(text = stringResource(actionBtn.first)) {
                        actionBtn.second()
                        activity.playSound(R.raw.btn)
                    }
                if(!game.started && game.pos.info.password != null)
                    ThickButton(onClick = { openRoomDlgOpener(Unit) }) {
                        Icon(painterResource(R.drawable.ico_unlock), null)
                        Text(stringResource(R.string.unlock))
                    }
            }
            ButtonTextOnly(text = stringResource(R.string.smile), onClick = emojiDlgOpener)
        }
    }
}