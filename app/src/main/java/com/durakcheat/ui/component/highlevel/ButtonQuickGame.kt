package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.durakcheat.R
import com.durakcheat.ui.component.leaf.PlayButton

@Composable
fun ButtonQuickGame(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = PlayButton(stringResource(R.string.quick_game), modifier, onClick = onClick)

@DPreview
@Composable
private fun PreviewButtonQuickGame() =
    ButtonQuickGame(Modifier.fillMaxWidth(0.9f)) {  }