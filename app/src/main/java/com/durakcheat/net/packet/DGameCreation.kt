package com.durakcheat.net.packet

import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DGameInfo
import com.durakcheat.net.json.DPlayerPosition

class DGameCreation (
    override val bet: Long,
    val password: String?,
    /** Maximum amount of players */
    val players: DPlayerPosition,
    override val deck: DDeck,
    override val sw: Boolean,
    override val ch: Boolean,
    override val dr: Boolean?,
    override val nb: Boolean,
    override val fast: Boolean
) : DGameInfo