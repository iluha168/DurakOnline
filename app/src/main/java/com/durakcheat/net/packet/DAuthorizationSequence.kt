package com.durakcheat.net.packet

import android.os.Build

class DConnect (
    val p: Int = 14,
    val d: String = (Build.BRAND+" "+ Build.DEVICE).uppercase(),
    val v: String = "1.9.15",
    val tz: String = "+00:00",
    val and: Int = Build.VERSION.SDK_INT,
    val pl: String = "android",
    val l: String = "en",
    val n: String = "durak.android"
)

class DKey (
    val key: String
)

class DHash (
    val hash: String
)

class DToken (
    val token: String
)

class DServerStatus (
    val id: String
)