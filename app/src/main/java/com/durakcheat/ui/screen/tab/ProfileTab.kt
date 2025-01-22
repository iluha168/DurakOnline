package com.durakcheat.ui.screen.tab

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.DClient
import com.durakcheat.net.packet.DUser
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.container.columnDialog
import com.durakcheat.ui.component.leaf.ButtonDelete
import com.durakcheat.ui.component.leaf.CashDisplay
import com.durakcheat.ui.component.leaf.CoinsDisplay
import com.durakcheat.ui.component.leaf.PlayButton
import com.durakcheat.ui.component.leaf.UserAvatar

@Composable
fun ProfileTab(activity: MainActivity){
    val userData = activity.client.user

    val rejoinFailedDlgOpener = columnDialog(
        title = stringResource(R.string.error),
        content = { -> Text(stringResource(R.string.rejoin_failed)) }
    )

    TitleText("Profile", Modifier.fillMaxWidth())
    Row {
        UserAvatar(
            user = DUser(
                id = activity.client.userID,
                name = userData.getOrDefault("name", stringResource(R.string.initializing)) as String,
                avatar = userData["avatar"] as String?,
            ),
            nav = activity.nav,
            size = 180.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            CashDisplay(activity.client.balance)
            CoinsDisplay(activity.client.coins)
        }
    }
    HorizontalDivider()
    with(activity.client){
        Column(Modifier.animateContentSize()) {
            PlayButton("Quick game", Modifier.fillMaxWidth()) {
                gameQuickStart()
            }
            lastGame.value?.let {
                PlayButton("Rejoin last game", Modifier.fillMaxWidth()) {
                    rejoinGame(it, failed = {
                        lastGame.value = null
                        rejoinFailedDlgOpener()
                    })
                }
            }
        }
    }
    HorizontalDivider()
    LazyListColumn(
        list = DClient.gameInvitations,
        key = { it.server+it.gameID },
        placeholder = { EmptySpaceFillerText(R.string.no_game_invites) }
    ){
        Row {
            PlayButton(it.alert, Modifier.weight(1f)) {
                activity.client.joinGame(it)
            }
            ButtonDelete {
                DClient.gameInvitations.remove(it)
            }
        }
    }
}