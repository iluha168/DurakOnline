package com.durakcheat.net.json

/** Signifies that this value is a player index in a game */
typealias DPlayerPosition = Int

const val PLAYERS_MIN: DPlayerPosition = 2
const val PLAYERS_MAX: DPlayerPosition = 6
val PLAYERS_INDICES = PLAYERS_MIN..PLAYERS_MAX