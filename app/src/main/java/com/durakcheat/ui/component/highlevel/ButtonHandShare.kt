package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.R
import com.durakcheat.ui.component.leaf.ThickButton

@Composable
fun ButtonHandShare(
    enabled: Boolean = true,
    onClick: () -> Unit
) = ThickButton(
    onClick = onClick,
    enabled = enabled,
    content = { Icon(Icons.Default.Share, stringResource(R.string.share_hand)) },
    slim = true, shape = RoundedCornerShape(10.dp),
    modifier = Modifier.width(40.dp)
)

@DPreview
@Composable
private fun PreviewHandShare() {
    Row {
        ButtonHandShare(false) {  }
        ButtonHandShare(true) {  }
    }
}