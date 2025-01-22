package com.durakcheat.net.packet

import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DPlayerPosition
import com.durakcheat.net.json.PLAYERS_INDICES

val booleans = listOf(false, true)
class DLookupStartOptions (
    val status: String = "open",
    val betMin: Long = 100,
    val betMax: Long = 100,
    /** List of allowed decks */
    val deck: List<DDeck> = DDeck.entries,
    /** List of allowed player amounts */
    val players: List<DPlayerPosition> = PLAYERS_INDICES.toList(),
    val fast: List<Boolean> = booleans,
    val sw: List<Boolean> = booleans,
    val ch: List<Boolean> = booleans,
    val nb: List<Boolean> = booleans,
    val dr: List<Boolean> = booleans,
    val pr: List<Boolean> = booleans,
)