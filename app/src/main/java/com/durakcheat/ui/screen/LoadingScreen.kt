package com.durakcheat.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.durakcheat.R
import com.durakcheat.net.json.DSmile
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.container.TitleText

@Preview
@Composable
fun LoadingScreen(stateString: Int = R.string.initializing){
    BackHandler {

    }

    val smileImg = remember {
        DSmile.vanillaSmiles.random().img
    }

    Box (modifier = Modifier.fillMaxSize()) {
        Icon (
            painterResource(smileImg),
            stringResource(stateString),
            Modifier
                .align(Alignment.Center)
                .alpha(0.3f)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f),
            tint = Color.Unspecified,
        )
        Rov (modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 10.dp)
        ) {
            TitleText(stringResource(stateString)+"...")
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}