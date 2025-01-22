package com.durakcheat.net.json

import androidx.annotation.DrawableRes
import com.durakcheat.R
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class DSmile (
    val netTranslation: Int,
    @DrawableRes val img: Int
) {
    SMILE(0, R.drawable.smile_0),
    WINK(1, R.drawable.smile_1),
    HAPPY(2, R.drawable.smile_2),
    LIKE(3, R.drawable.smile_3),
    TONGUE(4, R.drawable.smile_4),
    SUNGLASSES(5, R.drawable.smile_5),
    KISS(6, R.drawable.smile_6),
    ASTONISHED(7, R.drawable.smile_7),
    SAD(8, R.drawable.smile_8),
    TAPPING(9, R.drawable.smile_9),
    STARE(10, R.drawable.smile_10),
    BORED(11, R.drawable.smile_11),
    CONFIDENT(12, R.drawable.smile_12),
    ANGRY(13, R.drawable.smile_13),
    POKE(14, R.drawable.smile_14),
    FACEPALM(15, R.drawable.smile_15),
    WAVE(16, R.drawable.smile_16),
    UNAMUSED(17, R.drawable.smile_17),
    LAUGH(18, R.drawable.smile_18),
    GG(19, R.drawable.smile_19),
    WHISTLE(20, R.drawable.smile_20),
    GIFT(21, R.drawable.smile_21),
    CONFOUNDED(22, R.drawable.smile_22),
    HEAD_SCRATCH(23, R.drawable.smile_23),
    THINKING(24, R.drawable.smile_24),
    UNLOCK(99, R.drawable.smile_99),
    // Vanilla smiles end
    PARSE_ERROR(-1, R.drawable.smile_err);

    companion object {
        val vanillaSmiles: List<DSmile>
            get() = DSmile.entries.subList(SMILE.ordinal, UNLOCK.ordinal+1)
    }
}

object DSmileAdapter {
    @ToJson
    fun toJson(smile: DSmile) = smile.netTranslation

    @FromJson
    fun fromJson(id: Int) = DSmile.entries.find { it.netTranslation == id } ?: DSmile.PARSE_ERROR
}