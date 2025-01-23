package com.durakcheat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DSmile
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.net.packet.DUserInfoPersonal
import com.durakcheat.net.packet.MutableStateDFriendListEntry
import com.durakcheat.ui.animatePlacement
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.highlevel.ButtonHand
import com.durakcheat.ui.component.highlevel.ButtonHandShare
import com.durakcheat.ui.component.highlevel.ButtonQuickGame
import com.durakcheat.ui.component.highlevel.ListElementToken
import com.durakcheat.ui.component.highlevel.UserGameStatusIndicators
import com.durakcheat.ui.component.leaf.CardShape
import com.durakcheat.ui.component.leaf.DCardDisplay
import com.durakcheat.ui.component.leaf.NamedTextCounterRow
import com.durakcheat.ui.component.leaf.TextCounter
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.component.leaf.TransparentButtonIcon
import com.durakcheat.ui.component.leaf.UserAvatar
import com.durakcheat.ui.component.leaf.UserAvatarIcon
import com.durakcheat.ui.dialog.opaqueColorPickerDialog
import com.durakcheat.ui.noClip
import com.durakcheat.ui.stackableBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaletteScreen(activity: MainActivity){
    val palette = activity.themePalette.value!!
    val names = listOf(
        R.string.palette_primary, R.string.palette_secondary, R.string.palette_tertiary,
        R.string.palette_text1, R.string.palette_text2, R.string.palette_warning
    )
    val openers = mutableListOf<() -> Unit>()
    for(i in names.indices)
        openers.add(
            opaqueColorPickerDialog(title = stringResource(names[i])) {
                activity.themePalette.value = palette.replace(i, it)
            }.let {
                { it(palette[i]) }
            }
        )

    val maxW = Modifier.fillMaxWidth()
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TitleText(stringResource(R.string.screen_palette), maxW)
        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = maxW
        ) {
            val colorPickerWidth = 100.dp
            for (i in names.indices)
                ThickButton(
                    onClick = openers[i],
                    slim = true
                ) {
                    Column {
                        Text(
                            text = stringResource(names[i]),
                            modifier = Modifier.width(colorPickerWidth),
                            textAlign = TextAlign.Center
                        )
                        Box(Modifier
                            .background(palette[i])
                            .size(colorPickerWidth)
                        )
                    }
                }
        }
        TitleText(stringResource(R.string.examples), maxW)

        val user = DUserInfoPersonal.dummy
        val friend = MutableStateDFriendListEntry(
            DFriendListEntry(user, DFriendListEntryType.entries.random(), Math.random() > 0.5)
        )
        val trumpSuit = DCardSuit.entries.random()
        val randomBoolean = { Math.random() > 0.5 }

        ButtonQuickGame(maxW, openers[0])
        ListElementToken(openers[4], openers[0], openers[5], user, maxW)

        Rov(maxW) {
            Column(Modifier.width(avatarSize)) {
                UserAvatar(user, null, avatarSize)
                when ((Math.random() * 4).toInt()) {
                    0 -> LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    1 -> LinearProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                    2 -> LinearProgressIndicator(color = MaterialTheme.colorScheme.error)
                    3 -> LinearProgressIndicator(color = Color.Unspecified)
                }
            }
            ButtonHand(Math.random().times(15.0).toInt(), openers[2])
            ButtonHandShare(randomBoolean(), openers[4])
            UserGameStatusIndicators(
                isReady = randomBoolean(),
                hasGameStarted = randomBoolean(),
                hasDisconnected = randomBoolean(),
                wantsToSwap = randomBoolean(),
                onClickSwap = openers[4],
                winAmount = Math.random().times(1000.0).toLong(),
                isWinAmountShown = { randomBoolean() }
            )
        }

        ThickButton(
            modifier = maxW,
            slim = true,
            enabled = friend.raw.kind == DFriendListEntryType.FRIEND,
            onClick = {},
        ) {
            Rov(modifier = Modifier.noClip()) {
                UserAvatarIcon(friend.raw.user, null, 50.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = friend.raw.user.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        when (friend.raw.kind) {
                            DFriendListEntryType.REQUEST -> stringResource(R.string.user_friend_request_outbound)
                            DFriendListEntryType.INVITE -> stringResource(R.string.user_friend_request_inbound)
                            else -> ""
                        }
                    )
                }
                Row(
                    modifier = Modifier.background(if (friend.stateNew) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                ) {
                    when (friend.raw.kind) {
                        DFriendListEntryType.INVITE -> {
                            TransparentButtonIcon(Icons.Default.Check, stringResource(R.string.to_accept)) {}
                            TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.to_decline)) {}
                            TransparentButtonIcon(Icons.Default.Warning, stringResource(R.string.to_decline_always)) {}
                        }

                        DFriendListEntryType.FRIEND -> {
                            TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.delete)) {}
                            TransparentButtonIcon(Icons.Default.MailOutline, stringResource(R.string.user_chat_open)) {}
                        }

                        DFriendListEntryType.REQUEST ->
                            TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.user_friend_request_cancel)) {}

                        DFriendListEntryType.NOBODY ->
                            TransparentButtonIcon(Icons.Default.Add, stringResource(R.string.user_friend_request_send)) {}
                    }
                }
            }
        }

        Row {
            for (it in DSmile.vanillaSmiles.shuffled().subList(0,5))
                ThickButton(
                    onClick = openers[0],
                    slim = true,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painterResource(it.img), it.name,
                        modifier = Modifier.aspectRatio(1f),
                        tint = Color.Unspecified
                    )
                }
        }

        ThickButton(
            onClick = openers[1],
            color = MaterialTheme.colorScheme.secondary,
            modifier = maxW
        ) {
            Text(stringResource(R.string.to_place))
            DCardDisplay(cards = DDeck.entries.random().cards().shuffled().subList(0, 3), trumpSuit = trumpSuit)
        }

        Rov(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            NamedTextCounterRow(R.string.deck_left, (Math.random() * DDeck.entries.random().size).toInt())
            DCardDisplay(DCard(trumpSuit, DCardValue.entries.random()), trumpSuit = trumpSuit)
        }
        ThickButton(
            modifier = maxW,
            onClick = openers[1],
            color = MaterialTheme.colorScheme.secondary
        ) {
            Text(stringResource(R.string.to_check))
            TextCounter((Math.random() * DDeck.entries.random().size).toInt())
            Text(stringResource(R.string.cards_discarded).lowercase())
        }
        ThickButton(
            modifier = maxW,
            onClick = openers[1],
            color = MaterialTheme.colorScheme.secondary
        ) {
            Text(stringResource(R.string.to_check))
            TextCounter((Math.random() * DDeck.entries.random().size).toInt())
            Text(stringResource(R.string.cards_unseen).lowercase())
        }

        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = maxW,
        ) {
            val hand = DDeck.entries.random().cards().shuffled().subList(0, 2 + (Math.random() * 10).toInt())
            var handCardSelected by remember { mutableStateOf(hand.random()) }
            for (card in hand) {
                val isCardUseful = Math.random() > 0.5
                DCardDisplay(
                    card = card,
                    modifier = Modifier
                        .animatePlacement()
                        .stackableBorder(card == handCardSelected, MaterialTheme.colorScheme.primary, CardShape)
                        .stackableBorder(isCardUseful, MaterialTheme.colorScheme.tertiary, CardShape)
                        .clickable { handCardSelected = card },
                    trumpSuit = trumpSuit
                )
            }
        }
    }
}