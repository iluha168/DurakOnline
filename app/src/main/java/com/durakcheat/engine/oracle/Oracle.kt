package com.durakcheat.engine.oracle

import com.durakcheat.engine.DEMove
import com.durakcheat.engine.DEPosition
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DPlayerMode
import com.durakcheat.net.json.DPlayerPosition

data class Oracle (
    val pos: DEPosition
) {
    fun playerCardsProphecy(player: DEPosition.DEPlayer): Sequence<DCard> = sequence {
        val cards = player.cards
        val certainCards = cards.filterNotNull()
        yieldAll(certainCards)
        if (certainCards.isEmpty() && cards.isNotEmpty())
            yieldAll(pos.playersPossibleCards())
    }

    /** Lists all moves available to a player at the specified position. */
    fun movesProphecy(p: DPlayerPosition): Sequence<DEMove> = sequence {
        val player = pos.players[p]
        when (player.mode) {
            DPlayerMode.WIN,
            DPlayerMode.IDLE,
            DPlayerMode.BEAT_DONE,
            DPlayerMode.TAKE -> {}
            // This mode is impossible to get in Oracle, but may occur in the client
            DPlayerMode.CONFIRM -> {}
            // You can actually throw in in these modes, but allowing so in Oracle would result in infinite recursion
            DPlayerMode.DONE, DPlayerMode.PASS -> {}
            DPlayerMode.THROW_IN, DPlayerMode.THROW_IN_TAKE -> {
                if (pos.canThrowInAny()) {
                    val throwInAble = playerCardsProphecy(player).filter(pos::canThrowIn)
                    if (player.mode == DPlayerMode.THROW_IN) {
                        yield(DEMove.Done)
                        yieldAll(throwInAble.map (DEMove::ThrowIn))
                    } else {
                        yield(DEMove.Pass)
                        yieldAll(throwInAble.map (DEMove::AddTake))
                    }
                }
            }
            DPlayerMode.PLACE -> {
                yieldAll(playerCardsProphecy(player).map (DEMove::Place))
            }
            DPlayerMode.BEAT -> {
                yield(DEMove.Take)
                val myCards = playerCardsProphecy(player).toList()
                if (pos.canSwapMove(p)) {
                    val swapValue = pos.board[0].first.value
                    yieldAll(myCards
                        .filter { it.value == swapValue }
                        .map (DEMove::Swap)
                    )
                }
                for ((cardToBeat) in pos.board.filter { it.second == null }) {
                    for (myCard in myCards)
                        if (myCard.beats(cardToBeat, pos.trump.suit))
                            yield(DEMove.Beat(board = cardToBeat, beatWith = myCard))
                }
            }
        }
    }
}