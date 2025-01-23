package com.durakcheat.net.packet

import com.squareup.moshi.Json
import java.util.Date

open class DUser (
    val id: Long = 0,
    val name: String,
    val avatar: String? = null,
    val dtp: Date = Date(),
    val score: Long = 0L,
    val pw: Int = 0,
    val frame: String = "",
    val achieve: String = ""
)

open class DUserInfo (
    id: Long,
    name: String,
    avatar: String?,
    dtp: Date,
    pw: Int,
    frame: String,
    achieve: String,

    val ach: List<Int>,
    @Suppress("SpellCheckingInspection") @Json(name = "achc")
    val achC: Long,
    @Json(name = "t_bronze")
    val trophyBronze: Long,
    @Json(name = "t_silver")
    val trophySilver: Long,
    @Json(name = "t_gold")
    val trophyGold: Long,
    val wins: Long,
    @Json(name = "points_win")
    val pointsWin: Long,
    score: Long,
    val assets: List<String>,
    val achieves: List<String>,
    val coll: Map<String, DCollectible>
) : DUser(
    id, name, avatar, dtp, score, pw, frame, achieve
){
    class DCollectible (
        val group: String,
        val items: Map<Int, Int>
    )
}

class DUserInfoPersonal (
    name: String,
    avatar: String?,
    dtp: Date,
    score: Long,
    pw: Int,
    @Json(name = "new_msg")
    val hasNewMsg: Boolean,
    frame: String,
    achieve: String
): DUser(
    -1L, name, avatar, dtp, score, pw, frame, achieve
) {
    companion object {
        val dummy = DUserInfoPersonal(
            "Dummy", null, Date(0L), 1234L, 1, false, "", ""
        )
    }
}

class DUsersByTokensRequest (
    val tokens: List<String>
)

class DUsersByTokensResponse (
    val users: Map<String, DUserInfoPersonal>
)

class DUsersFindRequest (
    val name: String
)

class DUsersFindResponse (
    val users: List<DUser>
)