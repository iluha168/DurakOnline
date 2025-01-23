package com.durakcheat.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.net.packet.DUserInfo
import com.durakcheat.ui.component.container.ColumnDialog
import com.durakcheat.ui.component.leaf.ButtonTextOnly
import com.durakcheat.ui.component.leaf.UserAvatar

@Composable
fun ProfileDialogScreen (userID: Long, activity: MainActivity){
    var user by remember { mutableStateOf<DUserInfo?>(null) }
    LaunchedEffect(Unit) {
        user = activity.client.fetchUser(userID)
    }

    ColumnDialog(
        title = stringResource(R.string.profile),
        closer = { activity.nav.navigateUp() }
    ){
        Row {
            UserAvatar(user, null, 160.dp)
            Column {
                user?.run {
                    Text("WIP!\nRegistration date: $dtp")
                    // TODO achievements
                } ?: CircularProgressIndicator()
            }
        }
        // Action buttons row
        Row {
            if(userID != activity.client.userID) {
                // Friend button
                val friend = activity.client.friends[userID]
                val action = when (friend?.raw?.kind) {
                    DFriendListEntryType.FRIEND -> R.string.user_chat_open to { activity.nav.navigate("chat/$userID") }
                    DFriendListEntryType.REQUEST -> R.string.user_friend_request_cancel to { activity.client.friendDelete(friend.raw) }
                    DFriendListEntryType.INVITE -> R.string.user_friend_request_accept to { activity.client.friendRequestAccept(friend.raw) }
                    DFriendListEntryType.NOBODY, null -> R.string.user_friend_request_send to { activity.client.friendRequestSend(user!!) }
                }
                ButtonTextOnly(
                    text = stringResource(action.first),
                    modifier = Modifier.weight(1f),
                    onClick = action.second
                )
            }
        }
    }
}