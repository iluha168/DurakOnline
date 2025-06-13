package com.durakcheat.net

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.engine.DEMove
import com.durakcheat.engine.indexOfFirstOrNull
import com.durakcheat.engine.map
import com.durakcheat.engine.mapIndexed
import com.durakcheat.engine.with1Affected
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DMoshi
import com.durakcheat.net.json.DPlayerMode
import com.durakcheat.net.json.JSONPreference
import com.durakcheat.net.json.StateJSONPreference
import com.durakcheat.net.packet.DCardNotPositioned
import com.durakcheat.net.packet.DChatDataRequest
import com.durakcheat.net.packet.DConnect
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DFriendMessage
import com.durakcheat.net.packet.DFriendMessageDelete
import com.durakcheat.net.packet.DFriendMessageSend
import com.durakcheat.net.packet.DGameCreation
import com.durakcheat.net.packet.DGameInvitation
import com.durakcheat.net.packet.DGameJoin
import com.durakcheat.net.packet.DGameRejoin
import com.durakcheat.net.packet.DGameRejoinAble
import com.durakcheat.net.packet.DHandUpdate
import com.durakcheat.net.packet.DHash
import com.durakcheat.net.packet.DIdentifier
import com.durakcheat.net.packet.DLookupGame
import com.durakcheat.net.packet.DLookupStartOptions
import com.durakcheat.net.packet.DToken
import com.durakcheat.net.packet.DUser
import com.durakcheat.net.packet.DUserInfo
import com.durakcheat.net.packet.DUserInfoPersonal
import com.durakcheat.net.packet.DUsersByTokensRequest
import com.durakcheat.net.packet.DUsersFindRequest
import com.durakcheat.net.packet.MutableStateDFriendListEntry
import java.security.MessageDigest

class DClient(val activity: MainActivity) {
    internal val socket: DSocket = DSocket()

    var user = mutableStateMapOf<String, Any>()
    var game by mutableStateOf<DGameController?>(null)
    var friends = mutableStateMapOf<Long, MutableStateDFriendListEntry>()
    var lookup = mutableStateMapOf<Long, DLookupGame>()

    /** Some fields are made static to retain data after switching to a different [DClient] */
    companion object {
        var gameInvitations = mutableStateListOf<DGameInvitation>()
        val specialString = listOf(1,2,3,2,1).joinToString(""){ Char(it).toString() }

        enum class SpecialMessageTypes(id: Int) {
            SHARE_HAND(1)
            ;
            val c = Char(id)
            companion object {
                fun find(c: Char) = SpecialMessageTypes.entries.find { it.c == c }
            }
        }
    }

    /** Account preferences are not initialized for a token-less constructor. */
    private lateinit var accountPreferences: SharedPreferences
    lateinit var lastCreatedGame: JSONPreference<DGameCreation>
    lateinit var lastLookupFilter: JSONPreference<DLookupStartOptions>
    lateinit var lastGame: StateJSONPreference<DGameRejoinAble>

    private lateinit var tokenPrivate: String
    val hasToken: Boolean
        get() = ::tokenPrivate.isInitialized
    var token: String
        get() = tokenPrivate
        set(value) {
            tokenPrivate = value
            accountPreferences = activity.getSharedPreferences(java.net.URLEncoder.encode(value, "utf-8"), Context.MODE_PRIVATE)
            lastCreatedGame = JSONPreference(accountPreferences, "createGame", DGameCreation::class.java)
            lastLookupFilter = JSONPreference(accountPreferences, "filter", DLookupStartOptions::class.java)
            lastGame = StateJSONPreference(accountPreferences, "lastGame", DGameRejoinAble::class.java)
        }

    val coins: Long
        get() = user.getOrDefault("coins",0L).toString().toDouble().toLong()
    val balance: Long
        get() = user.getOrDefault("points",0L).toString().toDouble().toLong()
    val userID: Long
        get() = user.getOrDefault("id", 0L) as Long

    init {
        fun onGame(type: PacketType<*, EmptyDataPacket>, callback: DGameController.() -> Unit) =
            socket.on(type){
                game?.run(callback)
                    ?: Log.println(Log.WARN, "onGame", "Packet ${type.net} is ignored.")
            }
        fun <C : Any> onGame(type: PacketType<*, C>, callback: DGameController.(C) -> Unit) =
            socket.on(type){
                game?.run { callback(this, it) }
                    ?: Log.println(Log.WARN, "onGame", "Packet ${type.net} is ignored.")
            }

        socket.on(PacketType.USER_UPDATE) { uu ->
            if (uu.v != null) {
                user[uu.k] = uu.v
                if(uu.k == "new_msg" && uu.v == true)
                    activity.playNotificationSound()
            }
        }
        socket.on(PacketType.GAME_JOINED) {
            if(game == null) {
                game = DGameController(this, it)
                activity.runOnUiThread {
                    activity.nav.navigate("game")
                }
            } else if(game!!.info.id != it.id){
                game = DGameController(this, it)
            }
            saveLastGame()
        }
        onGame(PacketType.GAME_PUBLISHED) {
            info = info.copy(password = null)
        }
        socket.on(PacketType.ERROR) {
            if(it.code == "not_enough_points")
                gameClose()
            Log.println(Log.ERROR, "DSocket", it.code)
        }
        onGame(PacketType.BUTTON_READY_ON){
            btnReadyOn = true
        }
        onGame(PacketType.BUTTON_READY_OFF){
            btnReadyOn = false
            for(p in players) p.ready = false
        }
        onGame(PacketType.GAME_START){
            started = true
            for(p in players)
                p.wantsToSwap = false
            saveLastGame()
        }
        onGame(PacketType.GAME_RESET){
            reset()
        }
        onGame(PacketType.PLAYER_UPDATE){
            with(players[it.id]) {
                reset()
                user = it.user
            }
        }
        onGame(PacketType.PLAYER_UPDATE_CURRENT){
            myPosition = it.id
            saveLastGame()
            with(players[myPosition]) {
                user = it.user
                ready = false
            }
            for(p in players)
                p.wantsToSwap = false
        }
        onGame(PacketType.PLAYER_CONNECTED){
            players[it.id.toInt()].disconnected = false
        }
        onGame(PacketType.PLAYER_DISCONNECTED){
            players[it.id.toInt()].disconnected = true
        }
        onGame(PacketType.PLAYER_READY_ON){
            players[it.id.toInt()].ready = true
        }
        onGame(PacketType.PLAYER_READY_OFF){
            players[it.id.toInt()].ready = false
        }
        onGame(PacketType.PLAYER_MODES_UPDATE){ modesUpdate ->
            val prev = pos.players.joinToString { it.mode.toString() }
            val modesList = modesUpdate.modes.values
            var tmpPos = pos.copy(
                players = pos.players.mapIndexed { i, p ->
                    p.copy(mode = modesUpdate.modes[i]!!)
                },
                posAttacker = pos.posAttacker ?: modesList.indexOfFirstOrNull { it == DPlayerMode.PLACE     },
                posDefender = pos.posDefender ?: modesList.indexOfFirstOrNull { it == DPlayerMode.BEAT_DONE },
            )
            val curr = pos.players.joinToString { it.mode.toString() }
            if(prev != curr)

                Log.println(Log.WARN, "Player modes update", "Predict failed\nPrev: $prev\nCurr: $curr")

            if(pos.posAttacker == null && pos.posDefender == null){
                // This if statement executes only once per game start

                // If we have the second smallest trump card,
                // we can calculate who has the smallest trump card
                val smallestTrumpCard = DCard(
                    tmpPos.trump.suit,
                    DCardValue.entries[tmpPos.rules.deck.lowestCardValueIndex]
                )
                val secondSmallestTrumpCard = smallestTrumpCard.copy(
                    value = smallestTrumpCard.value.nextUp
                )
                if(secondSmallestTrumpCard in myCards)
                    tmpPos = tmpPos.copy(
                        players = tmpPos.players.with1Affected(tmpPos.posAttacker!!) {
                            copy(cards = cards - null + smallestTrumpCard)
                        }
                    )
            }
            pos = tmpPos
            // Calculate cards of other players
            if(pos.deckLeft <= 1 && pos.deckDiscardedAmount == pos.deckDiscarded.size) {
                val otherPlayer = (pos.players.filter { null in it.cards }).singleOrNull()
                if(otherPlayer != null)
                    pos = pos.copy(
                        players = pos.players.with1Affected({ it == otherPlayer }) {
                            copy(cards = cards.filterNotNull() + unknownCardCandidates)
                        }
                    )
            }
            if(myMode == DPlayerMode.CONFIRM)
                confirmTake()
        }
        socket.on(PacketType.FRIEND_LIST_UPDATE){
            if(friends[it.user.id]?.run {
                raw = it
                stateNew = it.new != false
            } == null)
                friends[it.user.id] = MutableStateDFriendListEntry(it)
        }
        socket.on(PacketType.FRIEND_LIST_DELETE){
            friends.remove(it.id)
        }
        onGame(PacketType.GAME_READY){
            acceptGameTimeout = it.timeout
        }
        onGame(PacketType.GAME_READY_TIMEOUT){
            gameClose()
        }
        onGame(PacketType.PLAYER_SWAP_REQUEST){
            players[it.id].wantsToSwap = true
        }
        onGame(PacketType.ME_POSITION_UPDATE){
            myPosition = it.id.toInt()
            saveLastGame()
        }
        onGame(PacketType.PLAYER_HAND_UPDATE){ newHand ->
            pos = pos.copy(players = pos.players.with1Affected(myPosition) {
                copy(cards = newHand.cards)
            })
        }
        onGame(PacketType.GAME_DECK_DRAW){
            //
        }
        onGame(PacketType.GAME_TURN_NEXT){ turn ->
            pos = pos.copy(
                deckLeft = turn.deck,
                trump = turn.trump,
                deckDiscardedAmount = turn.discard,
                board = turn.table?.toList() ?: pos.board,
            )
        }
        onGame(PacketType.PLAYER_THREW_IN){
            if(pos.rules.ch && !pos.canThrowIn(it.c))
                catchCheatPlace(it.c)
            pos = pos.applyMoveVirtually(it.id,
                when(pos.players[it.id].mode){
                    DPlayerMode.THROW_IN -> DEMove.ThrowIn(it.c)
                    DPlayerMode.PLACE -> DEMove.Place(it.c)
                    else -> throw Exception("Player cant place cards")
                }
            )
        }
        onGame(PacketType.PLAYER_THREW_IN_TAKE){
            if(pos.rules.ch && !pos.canThrowIn(it.c))
                catchCheatPlace(it.c)
            pos = pos.applyMoveVirtually(it.id, DEMove.AddTake(it.c))
        }
        onGame(PacketType.PLAYER_PASS_TURN){
            if(pos.rules.ch && !canSkipAround(it.c))
                catchCheatPlace(it.c)
            pos = pos.applyMoveVirtually(it.id, DEMove.Swap(it.c))
        }
        val reverseTfsCallback: DGameController.(DCardNotPositioned) -> Unit = {
            pos = pos.copy(
                players = pos.players.with1Affected(myPosition) {
                    copy(cards = cards + it.c)
                },
                board = pos.boardWithCardRemoved(it.c)
            )
        }
        onGame(PacketType.PLAYER_THREW_IN_ERROR, reverseTfsCallback)
        onGame(PacketType.PLAYER_THREW_IN_TAKE_ERROR, reverseTfsCallback)
        onGame(PacketType.PLAYER_PASS_TURN_ERROR, reverseTfsCallback)
        onGame(PacketType.PLAYER_BEAT_CARD){
            pos = pos.applyMoveVirtually(it.id, DEMove.Beat(
                board = it.c,
                beatWith = it.b
            ))
            if(pos.rules.ch && !it.b.beats(it.c, pos.trump.suit))
                catchCheatBeat(it.c, it.b)
        }
        onGame(PacketType.PLAYER_BEAT_CARD_ERROR){
            pos = pos.copy(
                players = pos.players.with1Affected(myPosition) {
                    copy(cards = cards + it.b)
                },
                board = pos.boardWithCardRemoved(it.b)
            )
        }
        onGame(PacketType.CHEAT_CAUGHT){
            activity.playSound(R.raw.nuh_uh)
            for(pair in it.c) pos = pos.copy(
                players = pos.players.with1Affected(pair.value) {
                    copy(cards = cards + pair.key)
                },
                board = pos.boardWithCardRemoved(pair.key)
            )
        }
        onGame(PacketType.PLAYER_THROW_IN_CANCEL){
            pos = pos.copy(
                players = pos.players.with1Affected(it.p) {
                    copy(cards = cards + it.c)
                },
                board = pos.boardWithCardRemoved(it.c)
            )
        }
        onGame(PacketType.PLAYER_BEAT_CANCEL){
            pos = pos.copy(
                players = pos.players.with1Affected(it.p) {
                    copy(cards = cards + it.b)
                },
                board = pos.boardWithCardRemoved(it.b)
            )
        }
        onGame(PacketType.GAME_TURN_END) { packet ->
            pos = if (packet.id != null)
                pos.withBoardTaken(packet.id)
            else
                pos.withBoardDiscarded()
        }
        onGame(PacketType.GAME_OVER){
            pos = pos.copy(
                players = pos.players.map { it.copy(mode = DPlayerMode.WIN) }
            )
        }
        socket.on(PacketType.LOOKUP_GAME_LIST){
            for(game in it.g)
                lookup[game.id] = game
        }
        socket.on(PacketType.LOOKUP_GAME_DELETED){
            lookup.remove(it.id)
        }
        socket.on(PacketType.LOOKUP_GAME_FOUND){
            lookup[it.id] = it
        }
        onGame(PacketType.GAME_STATUS){
            started = true
            pos = pos.copy(
                players = pos.players.mapIndexed { i, p ->
                    p.copy(cards = List(it.cards[i]!!) { null })
                }
            )
            for((p, w) in it.win)
                players[p].winAmount = w
            for(p in it.off)
                players[p].disconnected = true
        }
        onGame(PacketType.GAME_PLAYER_WIN){
            players[it.id].winAmount = it.value
            if(it.id == myPosition)
                activity.playSound(if(it.value < 0) R.raw.loss else R.raw.win)
        }
        socket.on(PacketType.GAME_INVITE){
            gameInvitations.add(it)
            activity.playNotificationSound()
        }
        onGame(PacketType.SMILE){
            players[it.p].smile = it.id
        }
        socket.on(PacketType.FRIEND_MSG_RECEIVED){
            val isIncoming = it.to == userID
            friends[if(isIncoming) it.from else it.to]?.run {
                if(it.msg.isEmpty())
                    chat.remove(it.id)
                else if(it.msg.startsWith(specialString)) {
                    friendMessageDelete(it)
                    if(isIncoming) {
                        val data = it.msg.substring(startIndex = specialString.length + 1)
                        when (SpecialMessageTypes.find(it.msg[specialString.length])) {
                            SpecialMessageTypes.SHARE_HAND -> game?.run {
                                val inGamePos = players.indexOfFirst { p -> p.user?.id == it.from }
                                pos = pos.copy(
                                    players = pos.players.with1Affected(inGamePos) {
                                        copy(cards = DMoshi.adapter(DHandUpdate::class.java).fromJson(data)!!.cards)
                                    }
                                )
                            }
                            null -> Unit
                        }
                    }
                } else
                    chat[it.id] = it
            }
        }
        socket.on(PacketType.SERVER_INFO) {
            activity.lastConnectedServer.str = it.id
        }
    }

    internal fun friendSpecialMessageSend(type: SpecialMessageTypes, data: String, friend: DFriendListEntry){
        friendMessageSend(specialString+type.c+data, friend)
    }

    suspend fun fetchUsersByTokens(tokens: List<String>): Map<String, DUserInfoPersonal> {
        return socket.fetch(
            PacketType.USERS_BY_TOKENS_REQUEST, DUsersByTokensRequest(tokens), PacketType.USERS_BY_TOKENS_RESPONSE,
            callback = { it.users.takeIf { u -> tokens.containsAll(u.keys) } }
        )
    }

    suspend fun fetchUser(id: Long): DUserInfo {
        return socket.fetch(
            PacketType.USER_INFO_REQUEST, DIdentifier(id), PacketType.USER_INFO_RESPONSE,
            callback = { it.takeIf {it.id == id} },
        )
    }

    suspend fun findUsers(name: String): List<DUser> {
        return socket.fetch(PacketType.USERS_FIND_REQUEST, DUsersFindRequest(name), PacketType.USERS_FIND_RESPONSE) { it.users }
    }

    fun markChatRead(friend: MutableStateDFriendListEntry){
        socket.send(PacketType.CHAT_MARK_READ, DIdentifier(friend.raw.user.id))
        friend.stateNew = false
    }

    suspend fun fetchMessages(friend: MutableStateDFriendListEntry, onAllFetched: () -> Unit){
        val userID = friend.raw.user.id
        val messages = socket.fetch(
            PacketType.CHAT_DATA_REQUEST, DChatDataRequest(userID, friend.chat.keys.minOrNull()),
            PacketType.CHAT_DATA_RESPONSE, callback = { it.takeIf { it.id == userID } }
        ).data
        if(messages.isEmpty())
            onAllFetched()
        else for(msg in messages)
            friend.chat[msg.id] = msg
    }

    fun friendMessageSend(msg: String, friend: DFriendListEntry){
        socket.send(PacketType.FRIEND_MSG_SEND, DFriendMessageSend(msg, friend.user.id))
    }
    fun friendMessageDelete(msg: DFriendMessage){
        socket.send(PacketType.FRIEND_MSG_DELETE, DFriendMessageDelete(msg.id))
    }
    fun friendRequestSend(user: DUser){
        socket.send(PacketType.FRIEND_REQUEST_SEND, DIdentifier(user.id))
    }
    fun friendRequestAccept(friend: DFriendListEntry){
        socket.send(PacketType.FRIEND_REQUEST_ACCEPT, DIdentifier(friend.user.id))
    }
    fun friendRequestDecline(friend: DFriendListEntry){
        socket.send(PacketType.FRIEND_REQUEST_DECLINE, DIdentifier(friend.user.id))
    }
    fun friendRequestIgnore(friend: DFriendListEntry){
        socket.send(PacketType.FRIEND_REQUEST_IGNORE, DIdentifier(friend.user.id))
    }
    fun friendDelete(friend: DFriendListEntry){
        socket.send(PacketType.FRIEND_DELETE, DIdentifier(friend.user.id))
    }

    private var didRefreshFriends: Boolean = false
    fun refreshFriends(){
        if(didRefreshFriends)
            return
        socket.send(PacketType.GET_FRIEND_LIST)
        didRefreshFriends = true
    }

    private fun saveLastGame(){
        lastGame.value = DGameRejoinAble(game!!.myPosition, game!!.info.id, activity.lastConnectedServer.str!!)
    }

    private fun gameClose(){
        if(game == null)
            return
        game = null
        activity.runOnUiThread {
            activity.nav.navigateUp()
        }
    }

    suspend fun getBets(): List<Long> {
        return socket.fetch(PacketType.GET_BETS, PacketType.BETS_UPDATE) { it.v }
    }

    fun gameQuickStart(){
        socket.send(PacketType.QUICK_GAME)
    }

    fun lookupStart(options: DLookupStartOptions){
        lastLookupFilter.value = options
        socket.send(PacketType.LOOKUP_START, options)
    }
    fun lookupStop(){
        socket.send(PacketType.LOOKUP_STOP)
        lookup.clear()
    }

    fun joinGame(game: DGameJoin){
        socket.send(PacketType.GAME_JOIN, game)
    }
    fun joinGame(game: DGameInvitation){
        activity.switchServer(game.server, token) {
            joinGame(DGameJoin(game.gameID, game.password))
        }
    }

    fun rejoinGame(game: DGameRejoinAble, failed: (() -> Unit)?) {
        activity.switchServer(game.serverID, token){
            if(failed != null)
                socket.onRawData(PacketType.ERROR){
                    val didFail = it.code == "game_not_found"
                    if(didFail) failed()
                    didFail
                }
            socket.send(PacketType.GAME_REJOIN, DGameRejoin(game.p, game.id))
        }
    }

    fun gameCreate(rules: DGameCreation){
        lastCreatedGame.value = rules
        socket.send(PacketType.GAME_CREATE, rules)
    }

    /** Is not safe to run on the UI thread */
    fun connect(servers: DServers, prepare: () -> Unit, callback: DClient.(isReconnecting: Boolean) -> Unit){
        socket.connect(
            prepare = prepare,
            getConn = {
                servers.user.run {
                    this[activity.lastConnectedServer.str] ?: values.random()
                }.connect()
            }
        ){
            // Login sequence code
            socket.once(PacketType.SIGN) { k ->
                @Suppress("SpellCheckingInspection")
                (k.key + "kdusyfngbfydtsttstcnsjsjdflflfl").toByteArray()
                    .let { MessageDigest.getInstance("MD5").digest(it) }
                    .let { Base64.encodeToString(it, Base64.NO_WRAP) }
                    .let { socket.send(PacketType.SIGN, DHash(it)) }
            }
            socket.once(PacketType.CONFIRMED) {
                callback(this, it)
            }
            // Start the confirm sequence
            socket.send(PacketType.CONNECT, DConnect())
        }
    }

    /** Must be [connect]ed beforehand! */
    suspend fun authorize(){
        if(!hasToken)
            throw Exception("Cannot authorize without a token")
        socket.fetch(
            PacketType.AUTHENTICATION, DToken(token),
            PacketType.AUTHORIZED
        ) { user["id"] = it.id }
        didRefreshFriends = false
        friends.clear()
    }

    fun close(){
        socket.close()
    }
}