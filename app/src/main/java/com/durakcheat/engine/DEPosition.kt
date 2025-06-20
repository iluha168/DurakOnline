package com.durakcheat.engine

import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardValue
import com.durakcheat.net.json.DGameRules
import com.durakcheat.net.json.DPlayerMode
import com.durakcheat.net.json.DPlayerPosition

data class DEPosition(
    val rules: DGameRules,
    val players: ArrayList<DEPlayer>,
    val posAttacker: DPlayerPosition?,
    val posDefender: DPlayerPosition?,
    val deckLeft: Int,
    val trump: DCard,
    val deckDiscarded: List<DCard>,
    val deckDiscardedAmount: Int,
    val board: List<Pair<DCard, DCard?>>,
) {
    data class DEPlayer (
        val mode: DPlayerMode = DPlayerMode.IDLE,
        val cards: List<DCard?> = List(6) { null },
    )

    operator fun List<DCard?>.minus(card: DCard?) = toMutableList().apply {
        if(!remove(card))
            if(!remove(null))
                throw Error("Failed to remove card $card from " + joinToString())
    }

    fun boardSpaceLeft() = (if(deckDiscardedAmount == 0) 5 else 6) - board.size

    fun defenderCardsRemaining(p: DPlayerPosition? = posDefender) = p?.run { players[this].cards.size - board.count { it.second == null } }

    fun nextPlayer(p: DPlayerPosition) = (
            ((p+1)..players.lastIndex).asSequence() + (0..<p).asSequence()
        ).find { players[it].cards.isNotEmpty() }

    fun previousPlayer(p: DPlayerPosition) = (
            ((p-1) downTo 0).asSequence() + (players.lastIndex downTo (p+1)).asSequence()
        ).find { players[it].cards.isNotEmpty() }

    fun canSwapMove(p: DPlayerPosition) = rules.sw
            && board.isNotEmpty()
            && board.all { it.second == null }
            && nextPlayer(p)?.let { players[it].cards.size > board.size } == true

    fun applyMoveVirtually(by: DPlayerPosition, move: DEMove): DEPosition {
        return when(move){
            DEMove.Done -> {
                passOrDoneHelper(by, DPlayerMode.DONE, DPlayerMode.PLACE)
                    .run {
                        if(players.none { it.mode == DPlayerMode.PLACE })
                            withBoardDiscarded()
                        else this
                    }
            }
            DEMove.Pass -> {
                passOrDoneHelper(by, DPlayerMode.PASS, DPlayerMode.THROW_IN_TAKE)
                    .run {
                        if(players.none { it.mode == DPlayerMode.THROW_IN_TAKE })
                            withBoardTaken(posDefender!!)
                        else this
                    }
            }
            DEMove.Take -> {
                copy(
                    players = players.mapIndexed { i, p ->
                        when {
                            i == by -> p.copy(mode = DPlayerMode.TAKE)
                            p.mode == DPlayerMode.THROW_IN -> p.copy(mode = DPlayerMode.THROW_IN_TAKE)
                            else -> p
                        }
                    }
                )
            }
            is DEMove.Beat -> {
                val newBoard = board.with1Affected({ it.first == move.board }) {
                    copy(second = move.beatWith)
                }
                val isBeatDone = newBoard.all { it.second != null }
                copy(
                    board = newBoard,
                    players = players.mapIndexed { i, p ->
                        when {
                            i == by -> p.copy(
                                cards = p.cards - move.beatWith,
                                mode = if(isBeatDone) DPlayerMode.BEAT_DONE else DPlayerMode.BEAT
                            )
                            isBeatDone && p.mode == DPlayerMode.THROW_IN ->
                                p.copy(mode = DPlayerMode.PLACE)
                            else -> p
                        }
                    }
                )
            }
            is DEMove.Place -> {
                val card = move.card
                copy(
                    board = board + (card to null),
                    players = players.mapIndexed { i, p ->
                        when {
                            i == by -> p.copy(cards = p.cards - card, mode = DPlayerMode.THROW_IN)
                            i == posDefender -> p.copy(mode = DPlayerMode.BEAT)
                            p.mode == DPlayerMode.DONE -> p.copy(mode = DPlayerMode.THROW_IN)
                            else -> p
                        }
                    }
                )
            }
            is DEMove.Swap -> {
                val card = move.card
                val futurePosDefender = nextPlayer(by)
                copy(
                    board = board + (card to null),
                    players = players.mapIndexed { i, p ->
                        when (i) {
                            by -> p.copy(cards = p.cards - card, mode = DPlayerMode.THROW_IN)
                            futurePosDefender -> p.copy(mode = DPlayerMode.BEAT)
                            posAttacker -> p.copy(mode = DPlayerMode.IDLE)
                            else -> p
                        }
                    },
                    posDefender = futurePosDefender,
                )
            }
            is DEMove.AddTake -> {
                val card = move.card
                copy(
                    board = board + (card to null),
                    players = players.with1Affected(by) {
                        copy(cards = cards - card)
                    }
                )
            }
            is DEMove.ThrowIn -> {
                val card = move.card
                copy(
                    board = board + (card to null),
                    players = players.with1Affected(by) {
                        copy(cards = cards - card, mode = DPlayerMode.THROW_IN)
                    }
                )
            }
        }
    }

    fun withBoardTaken(takerPos: DPlayerPosition): DEPosition {
        val boardCards = board.flatMap { listOfNotNull(it.first, it.second) }
        val futureAttacker = nextPlayer(takerPos)
        val futureDefender = futureAttacker?.let(::nextPlayer)
        return copy(
            board = emptyList(),
            players = players.mapIndexed { i, p ->
                when {
                    i == takerPos -> p.copy(mode = DPlayerMode.IDLE, cards = p.cards + boardCards)
                    i == futureAttacker -> p.copy(mode = DPlayerMode.PLACE)
                    i == futureDefender -> p.copy(mode = DPlayerMode.BEAT_DONE)
                    p.mode == DPlayerMode.WIN -> p
                    else -> p.copy(mode = DPlayerMode.IDLE)
                }
            },
            posAttacker = futureAttacker,
            posDefender = futureDefender,
        ).withCardsDrawn(posAttacker!!, posDefender!!)
    }

    fun withBoardDiscarded(): DEPosition {
        val boardCards = board.flatMap { listOfNotNull(it.first, it.second) }
        val futureAttacker = posDefender?.let {
            nextPlayer(it-1) // posDefender if Defender did not already win
        }
        val futureDefender = futureAttacker?.let(::nextPlayer)
        return copy(
            board = emptyList(),
            deckDiscarded = deckDiscarded + boardCards,
            deckDiscardedAmount = deckDiscardedAmount + boardCards.size,
            players = players.mapIndexed { i, p ->
                p.copy(mode = when {
                    i == futureAttacker -> DPlayerMode.PLACE
                    i == futureDefender -> DPlayerMode.BEAT_DONE
                    p.mode == DPlayerMode.WIN -> DPlayerMode.WIN
                    else -> DPlayerMode.IDLE
                })
            },
            posAttacker = futureAttacker,
            posDefender = futureDefender,
        ).withCardsDrawn(posAttacker!!, posDefender!!)
    }

    fun playersPossibleCards() = rules.deck.cards()
        .toMutableList()
        .apply {
            removeAll(deckDiscarded.toSet())
            for(stack in board){
                remove(stack.first)
                if(stack.second != null)
                    remove(stack.second!!)
            }
            for(p in players) removeAll(p.cards.filterNotNull().toSet())
            if(deckLeft > 0) remove(trump)
        }

    fun boardWithCardRemoved(card: DCard) = board.toMutableList().also {
        for(i in it.indices){
            val stack = it[i]
            if(stack.first == card){
                require(stack.second == null)
                it.removeAt(i)
                break
            }
            if(stack.second == card){
                it[i] = stack.copy(second = null)
                break
            }
        }
    }

    private fun withCardsDrawn(previousAttackerPos: DPlayerPosition, previousDefenderPos: DPlayerPosition): DEPosition {
        var cardsInDeck = deckLeft
        val newPlayers = ArrayList(players)
        for(p in sequence {
            // Duplicate code, but high performance
            for(p in previousAttackerPos..<players.size)
                if(p != previousDefenderPos)
                    yield(p)
            for(p in 0..<previousAttackerPos)
                if(p != previousDefenderPos)
                    yield(p)
            yield(previousDefenderPos)
        }) {
            val amount = (6 - newPlayers[p].cards.size).coerceIn(0, cardsInDeck)
            newPlayers[p] = newPlayers[p].copy(
                cards = newPlayers[p].cards + List(amount){ null }
            )
            cardsInDeck -= amount
            if(cardsInDeck <= 0){
                newPlayers[p] =
                    if(amount <= 0)
                        newPlayers[p].copy(mode = DPlayerMode.WIN)
                    else newPlayers[p].copy(
                        cards = newPlayers[p].cards.toMutableList().apply {
                            this[lastIndex] = trump
                        }
                )
            }
        }
        return copy(
            players = newPlayers,
            deckLeft = cardsInDeck,
            trump = if(cardsInDeck > 0) trump else trump.copy(value = DCardValue.EMPTY)
        )
    }

    private fun passOrDoneHelper(by: DPlayerPosition, mode: DPlayerMode, targetMode: DPlayerMode) = copy(
        players = players.run {
            if(rules.nb){
                val afterDefender = posDefender!!.let(::nextPlayer)
                val beforeDefender = posDefender.let(::previousPlayer)
                mapIndexed { i, p ->
                    when (i) {
                        by -> p.copy(mode = mode)
                        afterDefender, beforeDefender ->
                            if(p.mode == DPlayerMode.IDLE)
                                p.copy(mode = targetMode)
                            else p
                        else -> p
                    }
                }
            }
            else mapIndexed { i, p ->
                when {
                    i == by -> p.copy(mode = mode)
                    p.mode == DPlayerMode.IDLE -> p.copy(mode = targetMode)
                    else -> p
                }
            }
        }
    )
}