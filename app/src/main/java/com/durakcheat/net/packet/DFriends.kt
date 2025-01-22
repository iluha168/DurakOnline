package com.durakcheat.net.packet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.squareup.moshi.Json
import java.util.Date

enum class DFriendListEntryType {
    /** Incoming friend request */
    INVITE,
    /** Friend request accepted */
    FRIEND,
    /** Outgoing friend request */
    REQUEST,
    /** NOT AN API VALUE, this type is used to mark other users */
    NOBODY,
}

open class DFriendMessageSend(
    val msg: String,
    val to: Long
)

class DFriendMessageDelete(
    @Json(name = "msg_id")
    val msgID: Long
)

class DFriendMessage(
    val id: Long,
    val dtc: Date,
    val avatar: String?,
    msg: String,
    to: Long,
    val from: Long,
    val kind: String?,
    val payload: String?,
) : DFriendMessageSend(
    msg, to
)

class DFriendListEntry (
    val user: DUser,
    val kind: DFriendListEntryType = DFriendListEntryType.NOBODY,
    /** "true" or false, must be an api bug. */
    val new: Any = false,
)

class MutableStateDFriendListEntry (
    friend: DFriendListEntry
) {
    var raw by mutableStateOf(friend)
    val chat = mutableStateMapOf<Long, DFriendMessage>()
    var stateNew by mutableStateOf(friend.new != false)
}

class DChatDataRequest (
    /** Friend ID */
    val id: Long,
    @Json(name = "msg_id")
    val msgID: Long?
)

class DChatData (
    /** Friend ID */
    val id: Long,
    val begin: Boolean,
    val users: Map<Long, DUser>,
    val data: List<DFriendMessage>
)