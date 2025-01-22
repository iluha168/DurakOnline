package com.durakcheat.net.json

import androidx.compose.ui.text.intl.Locale

class DStringTranslatable (
    private val en: String,
    private val ru: String
){
    override fun toString(): String {
        if(Locale.current.language == "ru")
            return ru
        return en
    }
}