package com.durakcheat.net.json

import com.durakcheat.net.packet.DFriendListEntryType
import com.durakcheat.ui.theme.ThemePalette
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Date

val DMoshi: Moshi = Moshi.Builder()
    .add(Date::class.java, Rfc3339DateJsonAdapter())

    .add(DPlayerModeAdapter)
    .add(DPlayerModesUpdateAdapter)

    .add(DFriendListEntryType::class.java, EnumJsonAdapter.create(DFriendListEntryType::class.java))

    .add(DCardAdapter)
    .add(DDeckAdapter)
    .add(DSmileAdapter)

    .add(ThemePalette.MoshiAdapter)

    .add(KotlinJsonAdapterFactory())
    .build()