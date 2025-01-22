package com.durakcheat.net.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class DPlayerMode(val networkTranslation: Int) {
    /**
     * Whenever a player decides to take cards, players can throw-in.
     * In that case packet `f` is received for every throw-in.
     */
    THROW_IN_TAKE(0),
    /** The player has to place a card, their timer will tick. */
    PLACE(1),
    /** The player can place a card, their timer won't tick. */
    THROW_IN(2),
    /**
     * The player marked themself as 'Pass'.
     * When a player takes, everybody that can throw-in has to 'Pass' to confirm.
     * This can be set by server automatically when the board is full.
     */
    PASS(3),
    /**
     * The player marked themself as 'Done'.
     * When all players that can throw-in are 'Done', the cards are discarded and the next turn starts.
     * They still can throw-in cards.
     * */
    DONE(4),
    /** Players that have won act `IDLE`, but they won't get a turn again. The game is safe to leave at this state. */
    WIN(5),
    /** The player cannot act. */
    IDLE(6),
    /** The player decided to take the board instead of beating. */
    TAKE(7),
    /** The player beats cards, and all of them have been beaten. */
    BEAT_DONE(8),
    /** The player has to beat cards, their timer will tick. */
    BEAT(9),
    /** In this mode the player has to confirm all the cards before taking. (Tricks-enabled games only) */
    CONFIRM(10),
}

object DPlayerModeAdapter {
    @ToJson
    fun toJson(mode: DPlayerMode) = mode.networkTranslation

    @FromJson
    fun fromJson(data: Int) = DPlayerMode.entries[data]
}

class DPlayerModesUpdate (
    val modes: Map<DPlayerPosition, DPlayerMode>
)

object DPlayerModesUpdateAdapter {
    @ToJson
    fun toJson(modesUpdate: DPlayerModesUpdate) = modesUpdate.modes

    @FromJson
    fun fromJson(data: Map<DPlayerPosition, DPlayerMode>) = DPlayerModesUpdate(data)
}