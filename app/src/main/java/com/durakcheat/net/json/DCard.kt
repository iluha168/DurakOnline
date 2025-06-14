package com.durakcheat.net.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class DCardSuit(val netTranslation: Char){
    HEARTS('♥'),
    DIAMONDS('♦'),
    SPADES('♠'),
    CLUBS('♣')
}

enum class DCardValue(val netTranslation: String){
    EMPTY(""),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    val nextUp: DCardValue
        get() = DCardValue.entries[ordinal+1]
}

data class DCard(
    val suit: DCardSuit = DCardSuit.entries.first(),
    val value: DCardValue = DCardValue.entries.first(),
) : Comparable<DCard> {
    override fun toString(): String {
        return suit.netTranslation + value.netTranslation
    }

    constructor(str: String) : this(
        DCardSuit.entries.find { it.netTranslation == str[0] } ?: DCardSuit.SPADES,
        DCardValue.entries.find { it.netTranslation == str.substring(1) } ?: DCardValue.ACE,
    )

    infix fun beats(other: DCard): Boolean {
        return suit == other.suit && value.ordinal > other.value.ordinal
    }

    fun beats(other: DCard, trump: DCardSuit): Boolean {
        if(suit == trump && other.suit != trump)
            return true
        return this beats other
    }

    override fun compareTo(other: DCard): Int {
        return if(suit.ordinal == other.suit.ordinal)
            value.ordinal.compareTo(other.value.ordinal)
        else suit.ordinal.compareTo(other.suit.ordinal)
    }
}

object DCardAdapter {
    @ToJson
    fun toJson(card: DCard) = card.toString()

    @FromJson
    fun fromJson(data: String) = DCard(data)
}