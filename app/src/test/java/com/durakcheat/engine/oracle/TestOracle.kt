package com.durakcheat.engine.oracle

import com.durakcheat.engine.DEMove
import com.durakcheat.engine.DEPosition
import com.durakcheat.engine.TestDEPosition
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DPlayerMode
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class TestOracle {
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
}