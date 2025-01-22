package com.durakcheat.net.packet

import com.durakcheat.net.json.DPlayerPosition

class DGamePlayerUpdate (
    val id: DPlayerPosition,
    val user: DUser? = null,
    val swap: Boolean?
)