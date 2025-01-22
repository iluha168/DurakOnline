package com.durakcheat.net.json

import com.durakcheat.R
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class DDeck(val size: Int, val ico: Int) {
    DECK24(24, R.drawable.ico_deck_24),
    DECK36(36, R.drawable.ico_deck_36),
    DECK52(52, R.drawable.ico_deck_52);

    override fun toString(): String {
        return size.toString()
    }

    val suitSize = size/DCardSuit.entries.size
    val lowestCardValueIndex: Int = DCardValue.entries.lastIndex - suitSize + 1
    val avgCardValue = DCardValue.entries[lowestCardValueIndex + suitSize/2]

    fun cards(): List<DCard> {
        return DCardSuit.entries.flatMap { suit ->
            (lowestCardValueIndex..DCardValue.entries.lastIndex).map {
                DCard(suit, DCardValue.entries[it])
            }
        }
    }
}

object DDeckAdapter {
    @ToJson
    fun toJson(deck: DDeck) = deck.size

    @FromJson
    fun fromJson(size: Int) =
        DDeck.entries.find { it.size == size}
            ?: throw Exception("Could not find the deck with size $size")
}