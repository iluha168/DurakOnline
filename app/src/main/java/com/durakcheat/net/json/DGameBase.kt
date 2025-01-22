package com.durakcheat.net.json

interface DGameBase {
    val bet: Long
    val deck: DDeck
    /** Passing */
    val sw: Boolean
    /** Tricks/Cheats */
    val ch: Boolean
    /** Is draw enabled? */
    val dr: Boolean?
    /** Is throw-in neighbours-only? */
    val nb: Boolean
    val fast: Boolean
}