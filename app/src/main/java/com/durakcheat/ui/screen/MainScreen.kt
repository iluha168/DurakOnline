package com.durakcheat.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.DClient
import com.durakcheat.ui.component.container.RememberingAnimatedVisibility
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.leaf.NamedTextCounterRow
import com.durakcheat.ui.component.leaf.SelectionRow
import com.durakcheat.ui.component.leaf.ServerSwitcher
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.screen.tab.CreateTab
import com.durakcheat.ui.screen.tab.FriendsTab
import com.durakcheat.ui.screen.tab.ProfileTab
import com.durakcheat.ui.screen.tab.SearchTab

enum class MainScreenTab (
    val ico: ImageVector,
    val renderer: @Composable ColumnScope.(activity: MainActivity) -> Unit
){
    // ::Composable function references are not currently supported
    PROFILE(Icons.Default.Face, {ProfileTab(it)}),
    SEARCH(Icons.Default.Search, {SearchTab(it)}),
    FRIENDS(Icons.Default.MailOutline, {FriendsTab(it)}),
    CREATE(Icons.Default.Add, {CreateTab(it)});

    companion object {
        val default: MainScreenTab
            get() = PROFILE
    }
}

internal fun NavBackStackEntry.getTab(): MainScreenTab? =
    destination.route?.let { MainScreenTab.valueOf(it) }

@Composable
fun MainScreen(activity: MainActivity){
    val mainScreenNav = rememberNavController()

    fun navigateToTab(tab: MainScreenTab){
        val route = tab.name
        mainScreenNav.navigate(route) {
            popUpTo(route) { inclusive = true }
        }
    }

    Column {
        Rov {
            // Notifications row
            Row(Modifier.weight(1f)) {
                @Composable
                fun NotificationButton(
                    tab: MainScreenTab,
                    content: @Composable RowScope.() -> Unit
                ) = ThickButton(
                    onClick = { navigateToTab(tab) },
                    color = MaterialTheme.colorScheme.error,
                    content = content,
                    slim = true,
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
                RememberingAnimatedVisibility(DClient.gameInvitations.size, { it > 0 }) {
                    NotificationButton(tab = MainScreenTab.PROFILE) {
                        NamedTextCounterRow(R.string.game_invitations, it)
                    }
                }
                RememberingAnimatedVisibility(activity.client.user["new_msg"], { it == true }) {
                    NotificationButton(tab = MainScreenTab.FRIENDS) {
                        Icon(MainScreenTab.FRIENDS.ico, null)
                        Text(stringResource(R.string.new_message))
                    }
                }
            }
            ServerSwitcher(activity)
        }
        HorizontalDivider()
        NavHost(
            navController = mainScreenNav,
            startDestination = MainScreenTab.default.name,
            modifier = Modifier.weight(1f)
        ) {
            val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
                val iFrom = initialState.getTab()!!.ordinal
                val iTo = targetState.getTab()!!.ordinal
                slideInHorizontally {
                    if(iFrom > iTo) -it else it
                }
            }
            val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
                val iFrom = initialState.getTab()!!.ordinal
                val iTo = targetState.getTab()!!.ordinal
                slideOutHorizontally {
                    if(iFrom < iTo) -it else it
                }
            }
            for(tab in MainScreenTab.entries)
                composable(
                    route = tab.name,
                    enterTransition = enterTransition,
                    popEnterTransition = enterTransition,
                    exitTransition = exitTransition,
                    popExitTransition = exitTransition,
                ){
                    Column {
                        tab.renderer(this, activity)
                    }
                }
        }

        HorizontalDivider()
        // Navigation bar
        SelectionRow(
            values = MainScreenTab.entries,
            value = mainScreenNav.currentBackStackEntryAsState().value?.getTab() ?: MainScreenTab.default,
            onSelect = { navigateToTab(it) },
            item = { Icon(it.ico, it.name) },
            modifier = Modifier.padding(10.dp)
        )
    }
}
