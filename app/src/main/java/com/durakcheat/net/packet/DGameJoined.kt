package com.durakcheat.net.packet

import com.durakcheat.net.json.DDeck
import com.durakcheat.net.json.DGameBase

data class DGameJoined (
    val id: Long,
    val password: String?,

    val position: Int,
    val timeout: Long,

    val players: Int,
    override val bet: Long,
    override val deck: DDeck,
    override val sw: Boolean,
    override val ch: Boolean,
    override val dr: Boolean?,
    override val nb: Boolean,
    override val fast: Boolean
) : DGameBase