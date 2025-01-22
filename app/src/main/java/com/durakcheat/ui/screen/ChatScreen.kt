package com.durakcheat.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.leaf.ButtonIconOnly
import com.durakcheat.ui.component.leaf.TransparentButtonIcon
import com.durakcheat.ui.component.leaf.UserAvatar
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

const val MAX_MESSAGE_LENGTH = 255

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(userID: Long, activity: MainActivity){
    val friend = activity.client.friends[userID]
    if(friend == null) {
        activity.nav.navigateUp()
        return
    }

    val cs = rememberCoroutineScope()
    var canLoadMore by remember { mutableStateOf(true) }
    var input by remember { mutableStateOf("") }
    val loadMore = {
        cs.launch {
            activity.client.fetchMessages(friend) {
                canLoadMore = false
            }
        }
        Unit
    }
    LaunchedEffect(Unit) {
        loadMore()
        activity.client.markChatRead(friend)
    }
    DisposableEffect(true) {
        onDispose {
            activity.client.markChatRead(friend)
        }
    }

    Column {
        Rov(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            UserAvatar(friend.raw.user, activity.nav, avatarSize)
            if(canLoadMore)
                ButtonIconOnly(Icons.Default.Refresh, "Load more", onClick = loadMore)
        }
        HorizontalDivider()
        LazyListColumn(
            list = friend.chat.entries.toList().sortedBy { it.value.dtc },
            key = { it.value.id },
            placeholder = { EmptySpaceFillerText(R.string.empty_chat) },
            modifier = Modifier.weight(1f),
        ) { (_, msg) ->
            val isMyMsg = msg.from != userID
            Rov(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(),
                horizontalArrangement = if(isMyMsg) Arrangement.End else Arrangement.Start
            ) {
                val closeBtn = @Composable {
                    Column(
                        horizontalAlignment = if(isMyMsg) Alignment.End else Alignment.Start
                    ) {
                        TransparentButtonIcon(Icons.Default.Clear, stringResource(R.string.delete)) {
                            activity.client.friendMessageDelete(msg)
                        }
                        val localDT = msg.dtc.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        Text(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(localDT), fontSize = 10.sp)
                    }

                }
                if(isMyMsg) closeBtn()
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(10.dp),
                    colors = CardDefaults.cardColors(containerColor =
                        if(isMyMsg) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(msg.msg, Modifier.padding(10.dp))
                }
                if(!isMyMsg) closeBtn()
            }
        }

        Rov {
            TextField(
                value = input,
                onValueChange = {
                    input = it.slice(0..min(MAX_MESSAGE_LENGTH, it.length)-1)
                },
                placeholder = { Text("Enter a message...") },
                modifier = Modifier.weight(1f),
            )
            TransparentButtonIcon(Icons.AutoMirrored.Filled.Send, "Send") {
                activity.client.friendMessageSend(input, friend.raw)
                input = ""
            }
        }
    }
}