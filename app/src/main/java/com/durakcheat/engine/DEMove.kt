package com.durakcheat.engine

import com.durakcheat.net.json.DCard

sealed class DEMove {
    data object Err: DEMove()
    data object Wait: DEMove()
    data object Done: DEMove()
    data object Pass: DEMove()
    data object Take: DEMove()
    data class Beat(
        val board: DCard,
        val beatWith: DCard
    ): DEMove()
    data class Place(val card: DCard): DEMove()
    data class Swap(val card: DCard): DEMove()
    data class AddTake(val card: DCard): DEMove()
    data class ThrowIn(val card: DCard): DEMove()
}