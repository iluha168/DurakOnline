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
            DPlayerMode.THROW_IN -> {
                if (pos.board.all { it.second != null })
                    yield(DEMove.Done)
                if (pos.canThrowInAny()) yieldAll(
                    playerCardsProphecy(player).filter(pos::canThrowIn).map (DEMove::ThrowIn)
                )
            }
            DPlayerMode.THROW_IN_TAKE -> {
                yield(DEMove.Pass)
                if (pos.canThrowInAny()) yieldAll(
                    playerCardsProphecy(player).filter(pos::canThrowIn).map (DEMove::AddTake)
                )
            }
            DPlayerMode.PLACE -> {
                if (pos.board.isNotEmpty())
                    yield(DEMove.Done)
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

    private fun scoreOf(p: DPlayerPosition, previousPos: DEPosition, depthLeft: Int): Int {
        val player = pos.players[p]
        if (player.mode == DPlayerMode.WIN)
            return Int.MAX_VALUE
        val didNotWinAmount = pos.amountOfPlayersThatDidNotWin
        if (didNotWinAmount <= 1) return when {
            // The fool is `p`
            didNotWinAmount == 1 -> -1
            // Draw, not ideal
            pos.rules.dr == true -> Int.MAX_VALUE / previousPos.amountOfPlayersThatDidNotWin
            // Draw not enabled, the defender loses
            pos.posAttacker == p -> Int.MAX_VALUE
            else -> -1
        }
        if (depthLeft <= 0) {
            // Perform static evaluation
            return 2000000000
        }
        // 2 or more players that did not win, continue analysis

        val worstPossibleScoreForP = pos.players.indices.asSequence()
            .map { i -> bestMoveProphecy(i, depthLeft - 1) }
            .filterNotNull()
            .minOfOrNull { (move, oracleAfterMove) ->
                try { oracleAfterMove.scoreOf(p, pos, depthLeft - 1) }
                catch (e: Error) {
                    throw Error("From $this\nafter move $move", e)
                        .apply { stackTrace = emptyArray() }
                }
            }
        if (worstPossibleScoreForP == null)
            throw Error("Could not calculate scoreOf $this\n${
                pos.players.indices.asSequence()
                    .map { i -> i to bestMoveProphecy(i, depthLeft - 1) }
                    .joinToString("\n")
            }\n\n${
                pos.players.indices.asSequence()
                    .map { i -> i to movesProphecy(i).joinToString("\n\t") }
                    .joinToString("\n")
            }")
        return worstPossibleScoreForP - 1
    }

    fun bestMoveProphecy(p: DPlayerPosition, depthLeft: Int): Pair<DEMove, Oracle>? {
        val possibleMoves = movesProphecy(p).iterator()
        if (!possibleMoves.hasNext())
            return null

        val firstAvailableMove = possibleMoves.next()
        if (!possibleMoves.hasNext())
            return firstAvailableMove to Oracle(pos.applyMoveVirtually(p, firstAvailableMove))

        return movesProphecy(p)
            .map { it to Oracle(pos.applyMoveVirtually(p, it)) }
            .maxByOrNull { (move, oracle) ->
                try { oracle.scoreOf(p, pos, depthLeft - 1) }
                catch (e: Error) {
                    throw Error("From $this\nafter move $move by $p", e)
                        .apply { stackTrace = emptyArray() }
                }
            }
    }
}