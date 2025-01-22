package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.durakcheat.R
import com.durakcheat.net.packet.DUser

@Composable
fun UserAvatarIcon(user: DUser?, nav: NavHostController?, size: Dp, modifier: Modifier = Modifier){
    val userAvatarStr = stringResource(R.string.user_avatar)
    val sizeM = Modifier.size(size)
    user ?: return Icon(Icons.Default.AccountCircle, userAvatarStr, sizeM)
    @Composable
    fun content(){
        if(user.avatar == null)
            Icon(Icons.Default.AccountBox, userAvatarStr, sizeM)
        else
            SubcomposeAsyncImage(
                model = user.avatar,
                contentDescription = userAvatarStr,
                loading = { CircularProgressIndicator(sizeM) },
                modifier = sizeM,
            )
    }
    if(nav == null)
        content()
    else
        ThickButton(
            onClick = { nav.navigate("profileOf/"+user.id) },
            slim = true,
            modifier = modifier,
        ){
            content()
        }
}

@Composable
fun UserAvatar(user: DUser?, nav: NavHostController?, size: Dp, modifier: Modifier = Modifier){
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = user?.name ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.width(size),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        UserAvatarIcon(user, nav, size)
    }
}