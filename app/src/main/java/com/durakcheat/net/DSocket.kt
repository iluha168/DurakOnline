package com.durakcheat.net

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.durakcheat.net.json.DMoshi
import com.durakcheat.net.json.DPlayerModesUpdate
import com.durakcheat.net.packet.DBetsUpdate
import com.durakcheat.net.packet.DCardBeatNotPositioned
import com.durakcheat.net.packet.DCardBeatPositioned
import com.durakcheat.net.packet.DCardCancel
import com.durakcheat.net.packet.DCardCancelBeat
import com.durakcheat.net.packet.DCardNotPositioned
import com.durakcheat.net.packet.DCardPositioned
import com.durakcheat.net.packet.DChatData
import com.durakcheat.net.packet.DChatDataRequest
import com.durakcheat.net.packet.DCheatCaught
import com.durakcheat.net.packet.DConnect
import com.durakcheat.net.packet.DDeckDrawOrder
import com.durakcheat.net.packet.DErr
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DFriendMessage
import com.durakcheat.net.packet.DFriendMessageDelete
import com.durakcheat.net.packet.DFriendMessageSend
import com.durakcheat.net.packet.DGameCreation
import com.durakcheat.net.packet.DGameInvitation
import com.durakcheat.net.packet.DGameJoin
import com.durakcheat.net.packet.DGameJoined
import com.durakcheat.net.packet.DGameOver
import com.durakcheat.net.packet.DGamePlayerUpdate
import com.durakcheat.net.packet.DGameRejoin
import com.durakcheat.net.packet.DGameStatus
import com.durakcheat.net.packet.DGameWin
import com.durakcheat.net.packet.DHandUpdate
import com.durakcheat.net.packet.DHash
import com.durakcheat.net.packet.DIdentifier
import com.durakcheat.net.packet.DKey
import com.durakcheat.net.packet.DLookupGame
import com.durakcheat.net.packet.DLookupGameList
import com.durakcheat.net.packet.DLookupStartOptions
import com.durakcheat.net.packet.DNextTurn
import com.durakcheat.net.packet.DPlayerSmile
import com.durakcheat.net.packet.DPlayerSwapRequest
import com.durakcheat.net.packet.DServerStatus
import com.durakcheat.net.packet.DTimeout
import com.durakcheat.net.packet.DToken
import com.durakcheat.net.packet.DTurnEnd
import com.durakcheat.net.packet.DUserID
import com.durakcheat.net.packet.DUserInfo
import com.durakcheat.net.packet.DUserUpdate
import com.durakcheat.net.packet.DUsersByTokensRequest
import com.durakcheat.net.packet.DUsersByTokensResponse
import com.durakcheat.net.packet.DUsersFindRequest
import com.durakcheat.net.packet.DUsersFindResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class EmptyDataPacket

@Suppress("ClassName")
sealed class PacketType<S : Any, C: Any>(
    val net: String,
    val serverBound: Class<S>?,
    val clientBound: Class<C>?
) {
    data object CONNECT : PacketType<DConnect, Nothing>("c", DConnect::class.java, null)
    data object SIGN : PacketType<DHash, DKey>("sign", DHash::class.java, DKey::class.java)
    data object CONFIRMED : PacketType<Nothing, EmptyDataPacket>("confirmed", null, EmptyDataPacket::class.java)
    data object SERVER_INFO : PacketType<Nothing, DServerStatus>("server", null, DServerStatus::class.java)
    data object AUTHENTICATION : PacketType<DToken, Nothing>("auth", DToken::class.java, null)
    data object USER_UPDATE : PacketType<Nothing, DUserUpdate>("uu", null, DUserUpdate::class.java)
    data object AUTHORIZED : PacketType<Nothing, DIdentifier>("authorized", null, DIdentifier::class.java)
    data object GET_BETS : PacketType<EmptyDataPacket, Nothing>("gb", EmptyDataPacket::class.java, null)
    data object BETS_UPDATE : PacketType<Nothing, DBetsUpdate>("bets", null, DBetsUpdate::class.java)
    data object GAME_CREATE : PacketType<DGameCreation, Nothing>("create", DGameCreation::class.java, null)
    data object GAME_JOINED : PacketType<Nothing, DGameJoined>("game", null, DGameJoined::class.java)
    data object GAME_LEAVE : PacketType<DIdentifier, Nothing>("leave", DIdentifier::class.java, null)
    data object GAME_SURRENDER : PacketType<EmptyDataPacket, EmptyDataPacket>("surrender", EmptyDataPacket::class.java, EmptyDataPacket::class.java)
    data object GAME_PUBLISH : PacketType<EmptyDataPacket, Nothing>("game_publish", EmptyDataPacket::class.java, null)
    data object GAME_PUBLISHED : PacketType<Nothing, EmptyDataPacket>("game_public", null, EmptyDataPacket::class.java)
    data object ERROR : PacketType<Nothing, DErr>("err", null, DErr::class.java)
    data object ME_READY : PacketType<EmptyDataPacket, Nothing>("ready", EmptyDataPacket::class.java, null)
    data object BUTTON_READY_ON : PacketType<Nothing, EmptyDataPacket>("btn_ready_on", null, EmptyDataPacket::class.java)
    data object BUTTON_READY_OFF : PacketType<Nothing, EmptyDataPacket>("btn_ready_off", null, EmptyDataPacket::class.java)
    data object GAME_START : PacketType<Nothing, EmptyDataPacket>("game_start", null, EmptyDataPacket::class.java)
    data object PLAYER_UPDATE : PacketType<Nothing, DGamePlayerUpdate>("p", null, DGamePlayerUpdate::class.java)
    data object PLAYER_UPDATE_CURRENT : PacketType<Nothing, DGamePlayerUpdate>("cp", null, DGamePlayerUpdate::class.java)
    data object PLAYER_CONNECTED : PacketType<Nothing, DIdentifier>("p_on", null, DIdentifier::class.java)
    data object PLAYER_DISCONNECTED : PacketType<Nothing, DIdentifier>("p_off", null, DIdentifier::class.java)
    data object PLAYER_READY_ON : PacketType<Nothing, DIdentifier>("ready_on", null, DIdentifier::class.java)
    data object PLAYER_READY_OFF : PacketType<Nothing, DIdentifier>("ready_off", null, DIdentifier::class.java)
    data object PLAYER_MODES_UPDATE : PacketType<Nothing, DPlayerModesUpdate>("mode", null, DPlayerModesUpdate::class.java)
    data object GAME_RESET : PacketType<Nothing, EmptyDataPacket>("game_reset", null, EmptyDataPacket::class.java)
    data object FRIEND_LIST_UPDATE : PacketType<Nothing, DFriendListEntry>("fl_update", null, DFriendListEntry::class.java)
    data object FRIEND_LIST_DELETE : PacketType<Nothing, DIdentifier>("fl_delete", null, DIdentifier::class.java)
    data object FRIEND_GAME_INVITE : PacketType<DUserID, Nothing>("invite_to_game", DUserID::class.java, null)
    data object GET_FRIEND_LIST : PacketType<EmptyDataPacket, Nothing>("friend_list", EmptyDataPacket::class.java, null)
    data object GAME_READY : PacketType<Nothing, DTimeout>("game_ready", null, DTimeout::class.java)
    data object GAME_READY_TIMEOUT : PacketType<Nothing, EmptyDataPacket>("ready_timeout", null, EmptyDataPacket::class.java)
    data object PLAYER_HAND_UPDATE : PacketType<Nothing, DHandUpdate>("hand", null, DHandUpdate::class.java)
    data object GAME_DECK_DRAW : PacketType<Nothing, DDeckDrawOrder>("order", null, DDeckDrawOrder::class.java)
    data object PLAYER_SWAP : PacketType<DIdentifier, Nothing>("player_swap", DIdentifier::class.java, null)
    data object ME_PASS : PacketType<EmptyDataPacket, Nothing>("pass", EmptyDataPacket::class.java, null)
    data object ME_DONE : PacketType<EmptyDataPacket, Nothing>("done", EmptyDataPacket::class.java, null)
    data object ME_TAKE : PacketType<EmptyDataPacket, Nothing>("take", EmptyDataPacket::class.java, null)
    data object ME_CONFIRM_TAKE : PacketType<EmptyDataPacket, Nothing>("confirm", EmptyDataPacket::class.java, null)
    data object ME_POSITION_UPDATE : PacketType<Nothing, DIdentifier>("player_pos", null, DIdentifier::class.java)
    data object PLAYER_SWAP_REQUEST : PacketType<Nothing, DPlayerSwapRequest>("player_swap_request", null, DPlayerSwapRequest::class.java)
    data object GAME_TURN_NEXT : PacketType<Nothing, DNextTurn>("turn", null, DNextTurn::class.java)
    data object PLAYER_THREW_IN : PacketType<DCardNotPositioned, DCardPositioned>("t", DCardNotPositioned::class.java, DCardPositioned::class.java)
    data object PLAYER_THREW_IN_TAKE : PacketType<DCardNotPositioned, DCardPositioned>("f", DCardNotPositioned::class.java, DCardPositioned::class.java)
    data object PLAYER_PASS_TURN : PacketType<DCardNotPositioned, DCardPositioned>("s", DCardNotPositioned::class.java, DCardPositioned::class.java)
    data object PLAYER_THREW_IN_ERROR : PacketType<Nothing, DCardNotPositioned>("rt", null, DCardNotPositioned::class.java)
    data object PLAYER_THREW_IN_TAKE_ERROR : PacketType<Nothing, DCardNotPositioned>("rf", null, DCardNotPositioned::class.java)
    data object PLAYER_PASS_TURN_ERROR : PacketType<Nothing, DCardNotPositioned>("rs", null, DCardNotPositioned::class.java)
    data object PLAYER_THROW_IN_CANCEL : PacketType<Nothing, DCardCancel>("rct", null, DCardCancel::class.java)
    data object PLAYER_BEAT_CANCEL : PacketType<Nothing, DCardCancelBeat>("rcb", null, DCardCancelBeat::class.java)
    data object QUICK_GAME : PacketType<EmptyDataPacket, Nothing>("quick_game", EmptyDataPacket::class.java, null)
    data object PLAYER_BEAT_CARD : PacketType<DCardBeatNotPositioned, DCardBeatPositioned>("b", DCardBeatNotPositioned::class.java, DCardBeatPositioned::class.java)
    data object PLAYER_BEAT_CARD_ERROR : PacketType<Nothing, DCardBeatNotPositioned>("rb", null, DCardBeatNotPositioned::class.java)
    data object GAME_TURN_END : PacketType<Nothing, DTurnEnd>("end_turn", null, DTurnEnd::class.java)
    data object LOOKUP_START : PacketType<DLookupStartOptions, Nothing>("lookup_start", DLookupStartOptions::class.java, null)
    data object LOOKUP_STOP : PacketType<EmptyDataPacket, Nothing>("lookup_stop", EmptyDataPacket::class.java, null)
    data object LOOKUP_GAME_LIST : PacketType<Nothing, DLookupGameList>("gl", null, DLookupGameList::class.java)
    data object LOOKUP_GAME_DELETED : PacketType<Nothing, DIdentifier>("gd", null, DIdentifier::class.java)
    data object LOOKUP_GAME_FOUND : PacketType<Nothing, DLookupGame>("g", null, DLookupGame::class.java)
    data object GAME_JOIN : PacketType<DGameJoin, Nothing>("join", DGameJoin::class.java, null)
    data object GAME_REJOIN : PacketType<DGameRejoin, Nothing>("rejoin", DGameRejoin::class.java, null)
    data object GAME_STATUS : PacketType<Nothing, DGameStatus>("game_status", null, DGameStatus::class.java)
    data object GAME_PLAYER_WIN : PacketType<Nothing, DGameWin>("win", null, DGameWin::class.java)
    data object GAME_INVITE : PacketType<Nothing, DGameInvitation>("invite_to_game", null, DGameInvitation::class.java)
    data object CHEAT_CATCH_PLACE : PacketType<DCardNotPositioned, Nothing>("cht", DCardNotPositioned::class.java, null)
    data object CHEAT_CATCH_BEAT : PacketType<DCardBeatNotPositioned, Nothing>("chb", DCardBeatNotPositioned::class.java, null)
    data object CHEAT_CAUGHT : PacketType<Nothing, DCheatCaught>("chs", null, DCheatCaught::class.java)
    data object SMILE : PacketType<DIdentifier, DPlayerSmile>("smile", DIdentifier::class.java, DPlayerSmile::class.java)
    data object GAME_OVER : PacketType<Nothing, DGameOver>("game_over", null, DGameOver::class.java)
    data object FRIEND_REQUEST_ACCEPT : PacketType<DIdentifier, Nothing>("friend_accept", DIdentifier::class.java, null)
    data object FRIEND_REQUEST_DECLINE : PacketType<DIdentifier, Nothing>("friend_decline", DIdentifier::class.java, null)
    data object FRIEND_REQUEST_IGNORE : PacketType<DIdentifier, Nothing>("friend_ignore", DIdentifier::class.java, null)
    data object FRIEND_REQUEST_SEND : PacketType<DIdentifier, Nothing>("friend_request", DIdentifier::class.java, null)
    data object FRIEND_DELETE : PacketType<DIdentifier, Nothing>("friend_delete", DIdentifier::class.java, null)
    data object USERS_FIND_REQUEST : PacketType<DUsersFindRequest, Nothing>("users_find", DUsersFindRequest::class.java, null)
    data object USERS_FIND_RESPONSE : PacketType<Nothing, DUsersFindResponse>("users_find_result", null, DUsersFindResponse::class.java)
    data object USER_INFO_REQUEST : PacketType<DIdentifier, Nothing>("get_user_info", DIdentifier::class.java, null)
    data object USER_INFO_RESPONSE : PacketType<Nothing, DUserInfo>("user_info", null, DUserInfo::class.java)
    data object FRIEND_MSG_SEND : PacketType<DFriendMessageSend, Nothing>("send_user_msg", DFriendMessageSend::class.java, null)
    data object FRIEND_MSG_DELETE : PacketType<DFriendMessageDelete, Nothing>("delete_msg", DFriendMessageDelete::class.java, null)
    data object FRIEND_MSG_RECEIVED : PacketType<Nothing, DFriendMessage>("user_msg", null, DFriendMessage::class.java)
    data object CHAT_DATA_REQUEST : PacketType<DChatDataRequest, Nothing>("get_conversation", DChatDataRequest::class.java, null)
    data object CHAT_DATA_RESPONSE : PacketType<Nothing, DChatData>("conversation", null, DChatData::class.java)
    @Suppress("SpellCheckingInspection")         /* Seriously devs? \/ */
    data object CHAT_MARK_READ : PacketType<DIdentifier, Nothing>("msg_readed", DIdentifier::class.java, null)
    data object USERS_BY_TOKENS_REQUEST : PacketType<DUsersByTokensRequest, Nothing>("get_users_by_tokens", DUsersByTokensRequest::class.java, null)
    data object USERS_BY_TOKENS_RESPONSE : PacketType<Nothing, DUsersByTokensResponse>("users_by_tokens", null, DUsersByTokensResponse::class.java)
}

class DSocket {
    private val listeners = mutableMapOf<String, MutableSet<(String) -> Boolean>>()

    private lateinit var sock: Socket
    private lateinit var out: PrintWriter
    private var wlmHandler: Handler? = null
    
    private var isSafeToClose = false
    private var isProcessingIncomingPacket = false

    fun connect(prepare: () -> Unit, getConn: () -> Socket, onSendReady: (isReconnecting: Boolean) -> Unit) {
        prepare()
        val hasSockInit = ::sock.isInitialized
        sock = getConn()
        out = PrintWriter(sock.getOutputStream(), false)
        val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
        isSafeToClose = false

        thread(name = "Write loop") {
            Looper.prepare()
            wlmHandler = Handler(Looper.myLooper()!!)
            onSendReady(hasSockInit)
            Looper.loop()
            wlmHandler = null
        }

        thread(name = "Read loop") {
            try {
                while (true) {
                    isProcessingIncomingPacket = false
                    out.flush()
                    var data = reader.readLine()
                    if(data.isEmpty())
                        continue // Keep-alive packet received
                    isProcessingIncomingPacket = true
                    Log.println(Log.DEBUG, "DSocket", "⬇ $data")
                    var jsonBegin = data.indexOf('{')
                    if(jsonBegin == -1)
                        jsonBegin = data.length
                    val type = data.substring(0, jsonBegin)
                    data = data.substring(jsonBegin).ifEmpty { "{}" }
                    val bucket = listeners[type]
                    if(bucket == null) {
                        Log.println(Log.WARN, "DSocket", "No listeners set for packet type $type")
                        continue
                    }
                    with(bucket.iterator()) {
                        while(hasNext())
                            try {
                                if(next()(data))
                                    remove()
                            } catch (e: Throwable){
                                Log.println(Log.ERROR, "DSocket", "Exception while processing a callback of type $type:\n" + e.stackTraceToString())
                            }
                    }
                }
            } catch (e: SocketException) {
                if(!isSafeToClose){
                    Log.println(Log.ERROR, "DSocket", e.message +"\n"+ e.stackTraceToString())
                    wlmHandler?.looper?.quit()
                    while (true)
                        try {
                            connect(prepare, getConn, onSendReady)
                            break
                        } catch (e: ConnectException){
                            Log.println(Log.ERROR, "DSocket", "Reconnection failed: "+e.message)
                            Thread.sleep(4000)
                        }
                }
                return@thread
            }
        }
    }

    fun <S : Any> send(type: PacketType<S, *>, data: S){
        type.serverBound ?: throw Exception("Trying to send a non-server-bound packet "+type.net)
        wlmHandler?.post {
            send(type.net + DMoshi.adapter(type.serverBound).toJson(data))
        }
    }

    fun send(type: PacketType<EmptyDataPacket, *>){
        wlmHandler?.post {
            send(type.net)
        }
    }

    private fun send(text: String){
        Log.println(Log.DEBUG, "DSocket", "⬆ $text")
        out.write(text+'\n')
        if(!isProcessingIncomingPacket)
            out.flush()
    }

    fun <C : Any> onRawData(type: PacketType<*, C>, callback: (C) -> Boolean){
        onRawPacket(type) {
            callback(DMoshi.adapter(
                /** !! is safe because of the check in [onRawPacket] */
                type.clientBound!!
            ).fromJson(it)!!)
        }
    }

    private fun onRawEmptyDataPacket(type: PacketType<*, EmptyDataPacket>, callback: () -> Unit, remove: Boolean){
        onRawPacket(type) {
            callback()
            remove
        }
    }

    fun on(type: PacketType<*, EmptyDataPacket>, callback: () -> Unit) = onRawEmptyDataPacket(type, callback, false)
    fun once(type: PacketType<*, EmptyDataPacket>, callback: () -> Unit) = onRawEmptyDataPacket(type, callback, true)

    fun <C : Any> on(type: PacketType<*, C>, callback: (C) -> Unit) = onRawData(type){ callback(it); false }
    fun <C : Any> once(type: PacketType<*, C>, callback: (C) -> Unit) = onRawData(type){ callback(it); true }

    /** Callbacks return whether or not to remove themselves after firing. */
    private fun onRawPacket(type: PacketType<*, *>, callback: (String) -> Boolean){
        type.clientBound ?: throw Exception("Trying to receive a non-client-bound packet "+type.net)
        listeners.getOrPut(type.net, ::mutableSetOf) += callback
    }

    private suspend fun <C: Any, R: Any> fetchPrepareReceive(
        responseType: PacketType<*, C>, callback: (C) -> R?,
        send: () -> Unit,
    ) = suspendCoroutine { continuation ->
        wlmHandler?.post {
            onRawData(responseType) { res ->
                callback(res)?.also { continuation.resume(it) } != null
            }
            send()
        }
    }

    /** [callback] must return null if data does not match its criteria. */
    suspend fun <S: Any, C: Any, R: Any> fetch(
        queryType   : PacketType<S, *>, query: S,
        responseType: PacketType<*, C>, callback: (C) -> R?,
    ) = fetchPrepareReceive(responseType, callback){
        send(queryType, query)
    }

    /** [callback] must return null if data does not match its criteria. */
    suspend fun <C: Any, R: Any> fetch(
        queryType   : PacketType<EmptyDataPacket, *>,
        responseType: PacketType<*, C>, callback: (C) -> R?
    ) = fetchPrepareReceive(responseType, callback) {
        send(queryType)
    }

    fun close(){
        if(isSafeToClose)
            return // Close was called twice
        isSafeToClose = true
        wlmHandler!!.post {
            wlmHandler!!.looper.quitSafely()
            sock.close()
        }
    }
}