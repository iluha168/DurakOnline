package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.durakcheat.net.json.DSmile
import com.durakcheat.ui.component.leaf.ThickButton

@Composable
fun ButtonSmile(
    smile: DSmile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = ThickButton(
    modifier = modifier,
    onClick = onClick,
    slim = true,
    color = MaterialTheme.colorScheme.primary,
    shape = CircleShape
) {
    Icon(
        painterResource(smile.img), smile.name,
        modifier = Modifier.aspectRatio(1f),
        tint = Color.Unspecified
    )
}

@DPreview
@Composable
private fun PreviewButtonSmile() = LazyVerticalGrid(GridCells.Fixed(8)) {
    items(DSmile.vanillaSmiles) {
        ButtonSmile(it) { }
    }
}