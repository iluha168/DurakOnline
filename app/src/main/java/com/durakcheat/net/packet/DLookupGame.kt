package com.durakcheat.net.packet

import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DGameInfo
import com.durakcheat.net.json.DPlayerPosition

class DLookupGame (
    val id: Long,
    val name: String,

    /** Amount of players amount max */
    val p: DPlayerPosition,
    /** Amount of players currently in */
    val cp: DPlayerPosition,
    /** Amount of premium players currently in */
    val pc: DPlayerPosition,

    val pr: Boolean,
    override val bet: Long,
    override val deck: DDeck,
    override val sw: Boolean,
    override val ch: Boolean,
    override val dr: Boolean?,
    override val nb: Boolean,
    override val fast: Boolean
) : DGameInfo

class DLookupGameList (
    val g: List<DLookupGame>
)