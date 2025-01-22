package com.durakcheat.net.packet

import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DPlayerPosition

open class DCardNotPositioned (
    val c: DCard
)

class DCardPositioned (
    val id: DPlayerPosition,
    c: DCard
) : DCardNotPositioned(c)

open class DCardBeatNotPositioned (
    /** Card that was beaten */
    val c: DCard,
    /** Card that was used to beat */
    val b: DCard
)

class DCardBeatPositioned (
    val id: DPlayerPosition,
    c: DCard,
    b: DCard
) : DCardBeatNotPositioned(c, b)

open class DCardCancel (
    val p: DPlayerPosition,
    c: DCard
) : DCardNotPositioned(c)

class DCardCancelBeat (
    p: DPlayerPosition,
    c: DCard,
    val b: DCard
) : DCardCancel(p, c)