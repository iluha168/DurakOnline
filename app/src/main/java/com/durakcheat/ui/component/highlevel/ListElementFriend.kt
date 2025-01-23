package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.durakcheat.R
import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.net.packet.DUser
import com.durakcheat.net.packet.DUserInfoPersonal
import com.durakcheat.net.packet.booleans
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.component.leaf.TransparentButtonIcon
import com.durakcheat.ui.component.leaf.UserAvatarIcon
import com.durakcheat.ui.noClip

@Composable
fun ListElementFriend(
    modifier: Modifier = Modifier,

    user: DUser,
    nav: NavHostController?,

    friendKind: DFriendListEntryType,

    onChatOpen: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDeclineAlways: () -> Unit,
    onDelete: () -> Unit,
    onRequestSend: () -> Unit,

    attractAttention: Boolean,
) = ThickButton(
    modifier = modifier,
    slim = true,
    enabled = friendKind == DFriendListEntryType.FRIEND,
    onClick = onChatOpen,
) {
    Rov(modifier = Modifier.noClip()) {
        UserAvatarIcon(user, nav, 50.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            val tooltip = when (friendKind) {
                DFriendListEntryType.REQUEST -> R.string.user_friend_request_outbound
                DFriendListEntryType.INVITE -> R.string.user_friend_request_inbound
                else -> null
            }
            if(tooltip != null) {
                val attentionColor = if(attractAttention)
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Text(stringResource(tooltip), color = attentionColor)
            }
        }
        Row {
            when (friendKind) {
                DFriendListEntryType.INVITE -> {
                    TransparentButtonIcon(Icons.Default.Check, stringResource(R.string.to_accept), onClick = onAccept)
                    TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.to_decline), onClick = onDecline)
                    TransparentButtonIcon(Icons.Default.Warning, stringResource(R.string.to_decline_always), onClick = onDeclineAlways)
                }

                DFriendListEntryType.FRIEND -> {
                    TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.delete), onClick = onDelete)
                    TransparentButtonIcon(Icons.Default.MailOutline, stringResource(R.string.user_chat_open), onClick = onChatOpen)
                }

                DFriendListEntryType.REQUEST ->
                    TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.to_decline), onClick = onDelete)

                DFriendListEntryType.NOBODY ->
                    TransparentButtonIcon(Icons.Default.Add, stringResource(R.string.user_friend_request_send), onClick = onRequestSend)
            }
        }
    }
}

@DPreview
@Composable
private fun PreviewListElementFriend() = Column {
    for (attract in booleans) {
        for (type in DFriendListEntryType.entries)
            ListElementFriend(
                modifier = Modifier.fillMaxWidth(),
                user = DUserInfoPersonal.dummy,
                nav = null,
                friendKind = type,

                onChatOpen = {},
                onAccept = {},
                onDecline = {},
                onDeclineAlways = {},
                onDelete = {},
                onRequestSend = {},

                attractAttention = false,
            )
        Spacer(Modifier.height(30.dp))
    }
}