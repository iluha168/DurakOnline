package com.durakcheat.net.packet

import com.durakcheat.net.json.DPlayerPosition

class DGameStatus (
    /** Amounts of cards for each player position */
    val cards: Map<DPlayerPosition, Int>,
    /** How much have players won (if any) */
    val win: Map<DPlayerPosition, Long>,
    /** List of players who have disconnected */
    val off: List<DPlayerPosition>
)