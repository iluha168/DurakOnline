package com.durakcheat.ui.screen.tab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.net.packet.DUser
import com.durakcheat.net.packet.MutableStateDFriendListEntry
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.leaf.InputLineField
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.component.leaf.TransparentButtonIcon
import com.durakcheat.ui.component.leaf.UserAvatarIcon
import com.durakcheat.ui.dialog.confirmationDialog
import com.durakcheat.ui.noClip

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsTab(activity: MainActivity){
    var searchModeEnabled by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<DUser>()) }

    Rov(modifier = Modifier.animateContentSize()) {
        AnimatedContent(
            targetState = searchModeEnabled,
            label = "Title to input box",
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { state ->
            if(state)
                InputLineField("Enter a nickname...", autoClear = false) {
                    searchResults = activity.client.findUsers(it)
                }
            else
                TitleText("Friends")
        }

        TransparentButtonIcon(Icons.Default.Add, stringResource(R.string.find_players),
            Modifier.rotate(
                animateFloatAsState(if(searchModeEnabled) 45f else 0f, label = "Button rotation").value
            )
        ) {
            if(searchModeEnabled) searchResults = emptyList()
            searchModeEnabled = !searchModeEnabled
        }
    }

    val deleteDlgOpener = confirmationDialog(
        confirmBtnText = R.string.delete,
        titleText = R.string.delete_confirm,
        bodyText = stringResource(R.string.dlg_body_friend_delete),
        onConfirm = { friend: DFriendListEntry -> activity.client.friendDelete(friend) }
    )

    val ignoreDlgOpener = confirmationDialog(
        confirmBtnText = R.string.delete,
        titleText = R.string.delete_confirm,
        bodyText = stringResource(R.string.dlg_body_friend_ignore),
        onConfirm = { friend: DFriendListEntry -> activity.client.friendRequestIgnore(friend) }
    )

    LazyListColumn(
        list = with(activity.client.friends){
            if(searchModeEnabled)
                searchResults.map { this[it.id] ?: MutableStateDFriendListEntry(DFriendListEntry(it)) }
            else
                values.toList().sortedBy { it.raw.kind }
        },
        key = { it.raw.user.id },
        placeholder = { EmptySpaceFillerText(R.string.empty_users_list) },
    ) { friend ->
        val openChat = {
            activity.nav.navigate("chat/"+friend.raw.user.id)
        }
        ThickButton(
            modifier = Modifier
                .fillMaxWidth()
                .animateItemPlacement(),
            slim = true,
            enabled = friend.raw.kind == DFriendListEntryType.FRIEND,
            onClick = openChat,
        ) {
            Rov(modifier = Modifier.noClip()) {
                UserAvatarIcon(friend.raw.user, activity.nav, 50.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = friend.raw.user.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(when(friend.raw.kind){
                        DFriendListEntryType.REQUEST -> "Outgoing friend request"
                        DFriendListEntryType.INVITE -> "Incoming friend request"
                        else -> ""
                    })
                }
                Row(
                    modifier = Modifier.background(if(friend.stateNew) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                ) {
                    when(friend.raw.kind){
                        DFriendListEntryType.INVITE -> {
                            TransparentButtonIcon(Icons.Default.Check, "Accept") {
                                activity.client.friendRequestAccept(friend.raw)
                                friend.stateNew = false
                            }
                            TransparentButtonIcon(Icons.Default.Clear, "Decline") {
                                activity.client.friendRequestDecline(friend.raw)
                            }
                            TransparentButtonIcon(Icons.Default.Warning, "Never") {
                                ignoreDlgOpener(friend.raw)
                            }
                        }
                        DFriendListEntryType.FRIEND -> {
                            TransparentButtonIcon(Icons.Default.Clear, "Remove") {
                                deleteDlgOpener(friend.raw)
                            }
                            TransparentButtonIcon(Icons.Default.MailOutline, "Chat", onClick = openChat)
                        }
                        DFriendListEntryType.REQUEST ->
                            TransparentButtonIcon(Icons.Default.Clear, "Cancel request") {
                                deleteDlgOpener(friend.raw)
                            }
                        DFriendListEntryType.NOBODY ->
                            TransparentButtonIcon(Icons.Default.Add, "Send request") {
                                activity.client.friendRequestSend(friend.raw.user)
                            }
                    }
                }
            }
        }
    }
}