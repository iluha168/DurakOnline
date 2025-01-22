package com.durakcheat.net.packet

import com.durakcheat.net.json.DPlayerPosition

open class DGameRejoin (
    val p: DPlayerPosition,
    /** Game ID */
    val id: Long
)

class DGameRejoinAble (
    p: DPlayerPosition,
    id: Long,
    val serverID: String
) : DGameRejoin(
    p, id
)