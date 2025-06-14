package com.durakcheat.engine.oracle

import com.durakcheat.engine.DEMove
import com.durakcheat.engine.DEPosition
import com.durakcheat.engine.TestDEPosition
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DGameRules
import com.durakcheat.net.json.DPlayerMode
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class TestOracle {
    companion object {
        val posDoNotPlayTen = DEPosition(
            rules = DGameRules.Impl(
                deck = DDeck.DECK24, dr = false, ch = false, nb = true, sw = true, fast = true
            ),
            trump = DCard(DCardSuit.CLUBS, DCardValue.EMPTY),
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.PLACE, listOf(
                    DCard(DCardSuit.SPADES, DCardValue.TEN),
                    DCard(DCardSuit.CLUBS, DCardValue.JACK),
                    DCard(DCardSuit.HEARTS, DCardValue.JACK),
                    DCard(DCardSuit.DIAMONDS, DCardValue.JACK),
                    DCard(DCardSuit.CLUBS, DCardValue.KING),
                    DCard(DCardSuit.CLUBS, DCardValue.ACE),
                )),
                DEPosition.DEPlayer(DPlayerMode.BEAT_DONE, listOf(
                    DCard(DCardSuit.DIAMONDS, DCardValue.TEN),
                    DCard(DCardSuit.HEARTS, DCardValue.TEN),
                ))
            ),
            posAttacker = 0,
            posDefender = 1,
            board = emptyList(),
            deckDiscarded = emptyList(),
            deckLeft = 0,
            deckDiscardedAmount = 24 - 6 - 2,
        )
    }


    @Test
    fun testMovesProphecy_emptyPos() {
        val actualPossibleMoves = arrayOf<Array<DEMove>>(
            TestDEPosition.testPosition.rules.deck.cards()
                .filterNot { it == TestDEPosition.testPosition.trump }
                .map { DEMove.Place(it) }
                .toTypedArray(),
            emptyArray(),
            emptyArray()
        )
        val oracle = Oracle(TestDEPosition.testPosition)
        for ((p, moves) in actualPossibleMoves.withIndex())
            assertArrayEquals(moves, oracle.movesProphecy(p).toList().toTypedArray())
    }

    @Test
    fun testMovesProphecy_simplePos() {
        val actualPossibleMoves = arrayOf(
            arrayOf(
                DEMove.ThrowIn(DCard(DCardSuit.HEARTS, DCardValue.TEN))
            ),
            arrayOf(
                DEMove.Take,
                DEMove.Swap(DCard(DCardSuit.SPADES, DCardValue.TEN)),
                DEMove.Beat(DCard(DCardSuit.DIAMONDS, DCardValue.TEN), DCard(DCardSuit.DIAMONDS, DCardValue.ACE)),
                DEMove.Beat(DCard(DCardSuit.DIAMONDS, DCardValue.TEN), DCard(DCardSuit.CLUBS, DCardValue.EIGHT))
            ),
        )
        val oracle = Oracle(TestDEPosition.testPosition.copy(
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.THROW_IN, listOf(
                    DCard(DCardSuit.CLUBS, DCardValue.SEVEN),
                    DCard(DCardSuit.HEARTS, DCardValue.TEN),
                )),
                DEPosition.DEPlayer(DPlayerMode.BEAT, listOf(
                    DCard(DCardSuit.DIAMONDS, DCardValue.ACE),
                    DCard(DCardSuit.CLUBS, DCardValue.EIGHT),
                    DCard(DCardSuit.SPADES, DCardValue.TEN),
                )),
            ),
            board = listOf(
                DCard(DCardSuit.DIAMONDS, DCardValue.TEN) to null
            )
        ))
        for ((p, moves) in actualPossibleMoves.withIndex())
            assertArrayEquals("$p $moves", moves, oracle.movesProphecy(p).toList().toTypedArray())
    }

    @Test
    fun testBestMoveProphecy_simpleSwapSolution() {
        val oracle = Oracle(TestDEPosition.testPosition.copy(
            players = arrayListOf(
                DEPosition.DEPlayer(DPlayerMode.THROW_IN, listOf(
                    DCard(DCardSuit.CLUBS, DCardValue.SEVEN),
                    DCard(DCardSuit.HEARTS, DCardValue.JACK),
                )),
                DEPosition.DEPlayer(DPlayerMode.BEAT, listOf(
                    DCard(DCardSuit.SPADES, DCardValue.TEN),
                )),
            ),
            board = listOf(
                DCard(DCardSuit.DIAMONDS, DCardValue.TEN) to null
            ),
            deckDiscardedAmount = TestDEPosition.testPosition.rules.deck.size - 6,
            deckLeft = 0
        ))
        assertEquals(DEMove.Swap(DCard(DCardSuit.SPADES, DCardValue.TEN)), oracle.bestMoveProphecy(1, 1000)?.first)
    }

    @Test
    fun testBestMoveProphecy_avoidOpponentSwap() {
        val oracle = Oracle(posDoNotPlayTen)
        assertEquals(
            DEMove.Place(DCard(DCardSuit.CLUBS, DCardValue.JACK)),
            oracle.bestMoveProphecy(0, 20)?.first
        )
    }

    @Test
    fun testBestMoveProphecy_everything() {
        val oracle = Oracle(TestDEPosition.testPosition)
        // We are checking for no Exceptions
        oracle.bestMoveProphecy(0, 7)
    }
}