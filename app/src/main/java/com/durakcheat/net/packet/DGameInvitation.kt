package com.durakcheat.net.packet

import com.squareup.moshi.Json

class DGameInvitation (
    val alert: String,
    @Json(name = "game_id")
    val gameID: Long,
    val password: String?,
    val server: String
)