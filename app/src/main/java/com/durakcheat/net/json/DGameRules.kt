package com.durakcheat.net.json

interface DGameRules {
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

    data class Impl (
        override val deck: DDeck,
        override val sw: Boolean,
        override val ch: Boolean,
        override val dr: Boolean?,
        override val nb: Boolean,
        override val fast: Boolean
    ) : DGameRules
}