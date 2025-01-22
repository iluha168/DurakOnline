package com.durakcheat.net.packet

import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DPlayerPosition
import com.squareup.moshi.Json

class DCheatCaught (
    @Json(name = "ch")
    val cheater: DPlayerPosition,
    @Json(name = "sh")
    val sheriff: DPlayerPosition,
    /** Map of cards and their placers */
    val c: Map<DCard, DPlayerPosition>
)