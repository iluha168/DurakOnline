package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.DServers
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.lazyColumnDialog

val serverIconSizeModifier = Modifier.size(width = 70.dp, height = 44.dp)

@Composable
fun ServerImage(server: DServers.DServer, modifier: Modifier = Modifier){
    val combinedModifier = serverIconSizeModifier then modifier
    SubcomposeAsyncImage(
        model = server.image.toString(),
        contentDescription = server.name.toString(),
        modifier = combinedModifier,
        loading = { CircularProgressIndicator(combinedModifier) }
    )
}

@Composable
fun ServerSwitcher(activity: MainActivity, modifier: Modifier = Modifier) {
    activity.servers ?: return

    val currentServer = activity.servers!!.user[activity.lastConnectedServer.str]
    val dlgOpener = lazyColumnDialog<Unit>(stringResource(R.string.dlg_title_select_server)) { _, closer ->
        items(activity.servers!!.user.toList()) { (serverID, server) ->
            Rov(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.clickable {
                    closer()
                    activity.switchServer(serverID)
                }
            ) {
                Text(text = server.name.toString(), modifier = Modifier.weight(1f))
                ServerImage(server)
            }
        }
    }
    if(currentServer != null)
        ServerImage(currentServer, modifier.clickable {
            dlgOpener(Unit)
        })
}