package com.durakcheat.net.packet

import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DPlayerPosition

class DNextTurn (
    val deck: Int,
    val trump: DCard,
    val discard: Int,
    val table: Map<DCard, DCard?>?
)

class DTurnEnd (
    /** Position of the taking player, if any */
    val id: DPlayerPosition?
)