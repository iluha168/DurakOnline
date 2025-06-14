package com.durakcheat.engine

import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DGameRules
import com.durakcheat.net.json.DPlayerMode
import org.junit.Assert.assertEquals
import org.junit.Test

class TestDEPosition {
    companion object {
        val testPosition = DEPosition(
            rules = DGameRules.Impl(
                ch = false,
                dr = false,
                deck = DDeck.DECK24,
                sw = true,
                nb = false,
                fast = true,
            ),
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.PLACE),
                DEPosition.DEPlayer(DPlayerMode.BEAT_DONE),
                DEPosition.DEPlayer(DPlayerMode.IDLE)
            ),
            posAttacker = 0,
            posDefender = 1,
            deckLeft = DDeck.DECK24.size - 3*6,
            trump = DCard(DCardSuit.CLUBS, DCardValue.SIX),
            deckDiscarded = listOf(),
            deckDiscardedAmount = 0,
            board = listOf()
        )
    }

    @Test
    fun testBoardSpaceLeftAndDefenderCardsRemaining() {
        var position = testPosition
        assertEquals(5, position.boardSpaceLeft())
        assertEquals(6, position.defenderCardsRemaining())
        position = position.applyMoveVirtually(by = 0, DEMove.Place(DCard(DCardSuit.DIAMONDS, DCardValue.TEN)))
        assertEquals(4, position.boardSpaceLeft())
        assertEquals(5, position.defenderCardsRemaining())
        position = position.applyMoveVirtually(by = 1, DEMove.Beat(DCard(DCardSuit.DIAMONDS, DCardValue.TEN), DCard(DCardSuit.DIAMONDS, DCardValue.JACK)))
        assertEquals(4, position.boardSpaceLeft())
        assertEquals(5, position.defenderCardsRemaining())
        position = position.applyMoveVirtually(by = 0, DEMove.Done)
        position = position.applyMoveVirtually(by = 2, DEMove.Done)
        assertEquals(6, position.boardSpaceLeft())
        assertEquals(6, position.defenderCardsRemaining())
    }

    @Test
    fun testNextAndPreviousPlayer() {
        var position = testPosition
        for ((pIn, pOut) in sequenceOf(1, 2, 0).withIndex())
            assertEquals(pOut, position.nextPlayer(pIn))
        for ((pIn, pOut) in sequenceOf(2, 0, 1).withIndex())
            assertEquals(pOut, position.previousPlayer(pIn))
        position = position.copy(
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.WIN, cards = emptyList()),
                DEPosition.DEPlayer(DPlayerMode.THROW_IN),
                DEPosition.DEPlayer(DPlayerMode.WIN, cards = emptyList()),
                DEPosition.DEPlayer(DPlayerMode.BEAT_DONE),
            )
        )
        for ((pIn, pOut) in sequenceOf(1, 3, 3, 1).withIndex())
            assertEquals(pOut, position.nextPlayer(pIn))
        for ((pIn, pOut) in sequenceOf(3, 3, 1, 1).withIndex())
            assertEquals(pOut, position.previousPlayer(pIn))
    }

    @Test
    fun testApplyTake_autoPass() {
        val position = DEPosition(
            rules = DGameRules.Impl(deck=DDeck.DECK24, sw=true, ch=false, dr=true, nb=true, fast=true),
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.THROW_IN),
                DEPosition.DEPlayer(DPlayerMode.BEAT, List(3) { null })
            ),
            posAttacker = 0,
            posDefender = 1,
            deckLeft = 10,
            trump = DCard("♠A"),
            deckDiscarded = listOf("♦Q", "♠J").map(::DCard),
            deckDiscardedAmount = 2,
            board = listOf("♦J" to "♦K", "♣J" to "♠10", "♥J" to "♥Q", "♣9" to null, "♥10" to null, "♠9" to null)
                .map { (a,b) -> DCard(a) to b?.let(::DCard) }
        )
        assertEquals(
            DEPosition(
                rules = DGameRules.Impl(deck=DDeck.DECK24, sw=true, ch=false, dr=true, nb=true, fast=true),
                players = arrayListOf(
                    DEPosition.DEPlayer(DPlayerMode.PLACE),
                    DEPosition.DEPlayer(DPlayerMode.BEAT_DONE, List(3) { null } + listOf("♦J", "♦K", "♣J", "♠10", "♥J", "♥Q", "♣9", "♥10", "♠9").map(::DCard))
                ),
                posAttacker = 0,
                posDefender = 1,
                deckLeft = 10,
                trump = DCard("♠A"),
                deckDiscarded = listOf("♦Q", "♠J").map(::DCard),
                deckDiscardedAmount = 2,
                board = emptyList()
            ),
            position.applyMoveVirtually(1, DEMove.Take)
        )
    }
}