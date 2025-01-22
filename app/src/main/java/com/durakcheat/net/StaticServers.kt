package com.durakcheat.net

import com.durakcheat.net.json.DMoshi
import com.durakcheat.net.json.DStringTranslatable
import java.net.Socket
import java.net.URL

class DServers (
    val user: Map<String, DServer>,
) {
    class DServer (
        val name: DStringTranslatable,
        val image: DStringTranslatable,
        val host: String,
        val android: Int
    ) {
        fun connect(): Socket {
            return Socket(host, android)
        }
    }
}

fun getServerList(): DServers {
    val jsonData = URL("https://static.rstgames.com/durak/servers.json?"+System.currentTimeMillis()).readText()
    return DMoshi.adapter(DServers::class.java).fromJson(jsonData)
        ?: throw Exception("Failed to fetch servers")
}