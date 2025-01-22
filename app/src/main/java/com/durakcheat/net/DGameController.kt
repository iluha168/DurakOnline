package com.durakcheat.net

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.durakcheat.engine.ArrayList
import com.durakcheat.engine.DEMove
import com.durakcheat.engine.DEPosition
import com.durakcheat.engine.map
import com.durakcheat.engine.with1Affected
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DMoshi
import com.durakcheat.net.json.DPlayerMode
import com.durakcheat.net.json.DSmile
import com.durakcheat.net.packet.DCardBeatNotPositioned
import com.durakcheat.net.packet.DCardNotPositioned
import com.durakcheat.net.packet.DFriendListEntry
import com.durakcheat.net.packet.DGameJoined
import com.durakcheat.net.packet.DHandUpdate
import com.durakcheat.net.packet.DIdentifier
import com.durakcheat.net.packet.DUser
import com.durakcheat.net.packet.DUserID

class DGameController(
    val client: DClient,
    info: DGameJoined
) {
    var pos: DEPosition by mutableStateOf(DEPosition(
        info = info,
        players = ArrayList(info.players) {
            DEPosition.DEPlayer()
        },
        hand = emptyList(),
        posAttacker = null,
        posDefender = null,
        deckLeft = info.deck.size,
        trump = DCard(),
        deckDiscarded = emptyList(),
        deckDiscardedAmount = 0,
        board = emptyList()
    ))

    var myPosition: Int
        get() = pos.info.position
        set(value){ pos = pos.copy(info = pos.info.copy(position = value)) }

    var started by mutableStateOf(false)
    var btnReadyOn by mutableStateOf(false)
    var acceptGameTimeout by mutableLongStateOf(14000L)

    val players = Array(info.players){ DPlayerController() }

    class DPlayerController {
        var user by mutableStateOf<DUser?>(null)
        var disconnected by mutableStateOf(false)
        var ready by mutableStateOf(false)
        var wantsToSwap by mutableStateOf(false)
        var smile by mutableStateOf<DSmile?>(null)
        var winAmount by mutableLongStateOf(0L)

        fun reset(){
            wantsToSwap = false
            disconnected = false
            winAmount = 0L
        }
    }

    val clientPlayer get() = pos.players[myPosition]
    val nextPlayer get() = pos.nextPlayer(myPosition)?.let { pos.players[it] }
    val unknownCardCandidates: MutableList<DCard>
        get() = pos.playersPossibleCards().apply { removeAll(pos.hand.toSet()) }

    fun canThrowInAny(): Boolean {
        return pos.boardSpaceLeft() > 0 && pos.defenderCardsRemaining().let { it != null && it > 0 }
    }
    fun canThrowIn(card: DCard): Boolean {
        return pos.board.isEmpty() || pos.board.any { it.first.value == card.value || it.second?.value == card.value }
    }

    fun canSkipAround(card: DCard): Boolean {
        return pos.canSwapMove(myPosition) && pos.board[0].first.value == card.value
    }

    internal fun reset(){
        started = false
        pos = pos.copy(
            deckLeft = pos.info.deck.size,
            deckDiscarded = emptyList(),
            deckDiscardedAmount = 0,
            board = emptyList(),
            hand = emptyList(),
            players = pos.players.map {
                DEPosition.DEPlayer()
            },
            posDefender = null,
            posAttacker = null
        )
        for(p in players)
            p.reset()
    }

    fun friendShareHand(friend: DFriendListEntry){
        client.friendSpecialMessageSend(
            DClient.Companion.SpecialMessageTypes.SHARE_HAND,
            DMoshi.adapter(DHandUpdate::class.java).toJson(DHandUpdate(pos.hand.filterNotNull())),
            friend
        )
    }

    fun smile(smile: DSmile){
        client.socket.send(PacketType.SMILE, DIdentifier(smile.netTranslation.toLong()))
    }

    fun playMoveReal(move: DEMove){
        try {
            when (move) {
                is DEMove.AddTake -> playCardToTake(move.card)
                is DEMove.ThrowIn -> playCardThrowIn(move.card)
                is DEMove.Place -> placeCard(move.card)
                is DEMove.Beat -> beat(card = move.board, beatWith = move.beatWith)
                is DEMove.Swap -> passCard(move.card)
                DEMove.Done -> done()
                DEMove.Pass -> pass()
                DEMove.Take -> take()
                DEMove.Err -> throw move as Throwable
                DEMove.Wait -> {}
            }
        } catch (err: Throwable){
            Log.println(Log.ERROR, "PlayMoveReal", "Cannot play $move:\n${err.stackTraceToString()}")
        }
    }

    fun playCard(card: DCard){
        when(pos.players[myPosition].mode){
            DPlayerMode.THROW_IN_TAKE, DPlayerMode.PASS -> playCardToTake(card)
            DPlayerMode.THROW_IN -> playCardThrowIn(card)
            DPlayerMode.PLACE, DPlayerMode.DONE -> placeCard(card)
            else -> throw Exception("Cannot play cards")
        }
    }

    private fun placeCard(card: DCard){
        client.socket.send(PacketType.PLAYER_THREW_IN, DCardNotPositioned(card))
        pos = pos.applyMoveVirtually(myPosition, DEMove.Place(card))
    }

    private fun playCardThrowIn(card: DCard){
        client.socket.send(PacketType.PLAYER_THREW_IN, DCardNotPositioned(card))
        pos = pos.applyMoveVirtually(myPosition, DEMove.ThrowIn(card))
    }

    private fun playCardToTake(card: DCard) {
        client.socket.send(PacketType.PLAYER_THREW_IN_TAKE, DCardNotPositioned(card))
        pos = pos.applyMoveVirtually(myPosition, DEMove.AddTake(card))
    }

    fun beat(card: DCard, beatWith: DCard){
        pos = pos.applyMoveVirtually(myPosition, DEMove.Beat(board = card, beatWith))
        client.socket.send(PacketType.PLAYER_BEAT_CARD, DCardBeatNotPositioned(c = card, b = beatWith))
    }

    fun passCard(card: DCard){
        pos = pos.applyMoveVirtually(myPosition, DEMove.Swap(card))
        client.socket.send(PacketType.PLAYER_PASS_TURN, DCardNotPositioned(card))
    }

    fun catchCheatPlace(card: DCard){
        client.socket.send(PacketType.CHEAT_CATCH_PLACE, DCardNotPositioned(card))
    }
    fun catchCheatBeat(card: DCard, beatenWith: DCard){
        client.socket.send(PacketType.CHEAT_CATCH_BEAT, DCardBeatNotPositioned(c = card, b = beatenWith))
    }

    fun take(){
        pos = pos.applyMoveVirtually(myPosition, DEMove.Take)
        client.socket.send(PacketType.ME_TAKE)
    }

    fun invite(friend: DFriendListEntry){
        client.socket.send(PacketType.FRIEND_GAME_INVITE, DUserID(friend.user.id))
    }

    fun swap(pos: Int){
        client.socket.send(PacketType.PLAYER_SWAP, DIdentifier(pos.toLong()))
    }

    fun pass(){
        /** DO not apply a virtual move; that is done on [PacketType.GAME_TURN_END] */
        pos = pos.copy(players = pos.players.with1Affected(myPosition) {
            copy(mode = DPlayerMode.PASS)
        })
        client.socket.send(PacketType.ME_PASS)
    }

    fun done(){
        /** DO not apply a virtual move; that is done on [PacketType.GAME_TURN_END] */
        pos = pos.copy(players = pos.players.with1Affected(myPosition) {
            copy(mode = DPlayerMode.DONE)
        })
        client.socket.send(PacketType.ME_DONE)
    }

    fun confirmTake(){
        client.socket.send(PacketType.ME_CONFIRM_TAKE)
    }

    fun leave(){
        client.socket.send(PacketType.GAME_LEAVE, DIdentifier(pos.info.id))
        // Rejoin information is only stored when the game has started
        if(!started || players.singleOrNull { it.user != null } != null) {
            // It makes sense to remove lastGame when the only player in the room was the client
            client.lastGame.value = null
        }
        client.game = null
    }

    fun surrender(){
        client.socket.send(PacketType.GAME_SURRENDER)
    }

    fun publish(){
        client.socket.send(PacketType.GAME_PUBLISH)
    }

    fun ready(){
        players[myPosition].ready = true
        client.socket.send(PacketType.ME_READY)
    }
}