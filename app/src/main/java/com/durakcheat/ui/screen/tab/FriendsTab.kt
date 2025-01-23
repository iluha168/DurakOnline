package com.durakcheat.ui.screen.tab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DUser
import com.durakcheat.net.packet.MutableStateDFriendListEntry
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText
import com.durakcheat.ui.component.highlevel.ListElementFriend
import com.durakcheat.ui.component.leaf.InputLineField
import com.durakcheat.ui.component.leaf.TransparentButtonIcon
import com.durakcheat.ui.dialog.confirmationDialog

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
                InputLineField(stringResource(R.string.placeholder_user_search), autoClear = false) {
                    searchResults = activity.client.findUsers(it)
                }
            else
                TitleText(stringResource(R.string.friends))
        }

        TransparentButtonIcon(Icons.Default.Add, stringResource(R.string.find_users),
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
        ListElementFriend(
            modifier = Modifier
                .fillMaxWidth()
                .animateItemPlacement(),
            user = friend.raw.user,
            nav = activity.nav,
            friendKind = friend.raw.kind,

            onChatOpen = { activity.nav.navigate("chat/"+friend.raw.user.id) },
            onAccept = {
                activity.client.friendRequestAccept(friend.raw)
                friend.stateNew = false
            },
            onDecline = { activity.client.friendRequestDecline(friend.raw) },
            onDeclineAlways = { ignoreDlgOpener(friend.raw) },
            onDelete = { deleteDlgOpener(friend.raw) },
            onRequestSend = { activity.client.friendRequestSend(friend.raw.user) },

            attractAttention = friend.stateNew,
        )
    }
}