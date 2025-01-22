package com.durakcheat.ui.component.leaf

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.durakcheat.R

@Composable
fun ButtonTextOnly(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color? = null, onClick: () -> Unit){
    ThickButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        color = color
    ){
        Text(text = text)
    }
}

@Composable
fun TransparentButtonIcon(img: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, onClick: () -> Unit){
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(img, contentDescription)
    }
}

@Composable
fun ButtonIconOnly(img: Painter, contentDescription: String?, modifier: Modifier = Modifier, color: Color? = null, onClick: () -> Unit){
    ThickButton(
        onClick = onClick,
        modifier = modifier,
        color = color
    ){
        Icon(img, contentDescription)
    }
}

@Composable
fun ButtonIconOnly(@DrawableRes img: Int, contentDescription: String?, modifier: Modifier = Modifier, color: Color? = null, onClick: () -> Unit){
    ButtonIconOnly(painterResource(img), contentDescription, modifier, color, onClick)
}

@Composable
fun ButtonIconOnly(img: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, color: Color? = null, onClick: () -> Unit){
    ButtonIconOnly(rememberVectorPainter(img), contentDescription, modifier, color, onClick)
}


@Composable
fun PlayButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit){
    ThickButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(Icons.Default.PlayArrow, "Play")
        Text(text = text)
    }
}

@Composable
fun ButtonDelete(modifier: Modifier = Modifier, onClick: () -> Unit){
    ThickButton(
        onClick = onClick,
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
    ){
        Icon(Icons.Default.Delete, stringResource(R.string.delete))
    }
}