package com.durakcheat

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RawRes
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.durakcheat.net.DClient
import com.durakcheat.net.DServers
import com.durakcheat.net.getServerList
import com.durakcheat.net.json.StateJSONPreference
import com.durakcheat.net.json.StateStringPreference
import com.durakcheat.ui.dialog.ProfileDialogScreen
import com.durakcheat.ui.screen.ChatScreen
import com.durakcheat.ui.screen.ChooseAccountScreen
import com.durakcheat.ui.screen.GameScreen
import com.durakcheat.ui.screen.LoadingScreen
import com.durakcheat.ui.screen.MainScreen
import com.durakcheat.ui.screen.PaletteScreen
import com.durakcheat.ui.theme.DurakCheatTheme
import com.durakcheat.ui.theme.ThemePalette
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private lateinit var preferences: SharedPreferences
    lateinit var lastConnectedServer: StateStringPreference
    lateinit var themePalette: StateJSONPreference<ThemePalette>

    var servers: DServers? = null
    var client: DClient = DClient(this)

    lateinit var nav: NavHostController
    private var loadingState by mutableIntStateOf(R.string.initializing)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("Durak", MODE_PRIVATE)
        lastConnectedServer = StateStringPreference(preferences, "serverID")
        val tokensState = mutableStateListOf(
            *preferences.getStringSet("tokens", emptySet())!!.toTypedArray()
        )
        themePalette = StateJSONPreference(preferences, "palette", ThemePalette::class.java)

        val myBuilder: NavGraphBuilder.() -> Unit = {
            composable("login") {
                ChooseAccountScreen(this@MainActivity, tokensState) {
                    preferences.edit().putStringSet("tokens", tokensState.toSet()).apply()
                    nav.navigate("main") //NAV: login -> main
                    authorizeClient(it)
                }
            }
            composable("loading") { LoadingScreen(loadingState) }
            composable("main") { MainScreen(this@MainActivity) }
            composable("game") { GameScreen(this@MainActivity) }
            dialog(
                "profileOf/{id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType
                })
            ) {
                ProfileDialogScreen(it.arguments!!.getLong("id"), this@MainActivity)
            }
            composable(
                "chat/{id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType
                }),
                enterTransition = { slideInVertically { it } },
                exitTransition = { slideOutVertically { it } }
            ) {
                ChatScreen(it.arguments!!.getLong("id"), this@MainActivity)
            }
            composable("palette"){ PaletteScreen(this@MainActivity) }
        }

        setContent {
            nav = rememberNavController()
            DurakCheatTheme(themePalette) {
                Scaffold { paddingValues ->
                    NavHost(
                        navController = nav,
                        startDestination = "loading",
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        builder = myBuilder
                    )
                }
            }
        }

        //NAV: loading
        loadingState = R.string.load_servers
        thread {
            while(true)
                try {
                    servers = getServerList()
                    break
                } catch (e: Throwable){
                    Thread.sleep(3000)
                }
            runOnUiThread {
                nav.popBackStack(nav.graph.startDestinationId, true)
                nav.graph.setStartDestination("login")
                nav.navigate("login") //NAV: login
                confirmClient {
                    if (client.hasToken) {
                        authorizeClient(client.token){
                            val lastGame = client.lastGame.value
                            if(it && lastGame != null){
                                client.rejoinGame(lastGame){
                                    client.lastGame.value = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Safe to run on the UI thread */
    fun switchServer(serverID: String, token: String = client.token, callback: DClient.() -> Unit = {}) {
        if (serverID != lastConnectedServer.str) thread {
            lastConnectedServer.str = serverID
            client.close()
            client = DClient(this)
            client.token = token
            confirmClient {
                authorizeClient(client.token){
                    val lastGame = client.lastGame.value
                    if(it && lastGame != null){
                        client.rejoinGame(lastGame){
                            client.lastGame.value = null
                        }
                    }
                    callback(client)
                }
            }
        } else
            callback(client)
    }

    /** Safe to run on the UI thread */
    private fun confirmClient(callback: (isReconnecting: Boolean) -> Unit) {
        thread {
            client.connect(
                prepare = {
                    runOnUiThread {
                        loadingState = R.string.connecting
                        nav.navigate("loading"){
                            popUpTo("loading") { inclusive = true }
                        } //NAV: * -> loading
                    }
                },
                servers = servers!!
            ) {
                runOnUiThread {
                    nav.navigateUp() //NAV: *
                    callback(it)
                }
            }
        }
    }

    /** Must be run on the UI thread */
    private fun authorizeClient(token: String, callback: () -> Unit = {}) {
        loadingState = R.string.logging_in
        nav.navigate("loading") //NAV: * -> loading
        client.token = token
        thread(isDaemon = true) {
            runBlocking {
                client.authorize()
            }
            runOnUiThread {
                client.refreshFriends()
                nav.navigateUp() //NAV: *
                callback()
            }
        }
    }

    fun playNotificationSound(){
        playSound(R.raw.new_msg)
    }
    fun playSound(@RawRes id: Int){
        val mp = MediaPlayer.create(this, id)
        mp.setOnCompletionListener {
            it.stop()
            it.reset()
            it.release()
        }
        mp.start()
    }
}