package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.durakcheat.R
import com.durakcheat.net.packet.DUserInfoPersonal
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.component.leaf.ButtonDelete
import com.durakcheat.ui.component.leaf.ButtonIconOnly
import com.durakcheat.ui.component.leaf.ThickButton
import com.durakcheat.ui.component.leaf.UserAvatarIcon

@Composable
fun ListElementToken(
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    user: DUserInfoPersonal,
    modifier: Modifier = Modifier,
) = ThickButton(
    onClick = onClick,
    modifier = modifier,
    slim = true,
) {
    Rov(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UserAvatarIcon(user, nav = null, 70.dp)
        Text(
            text = user.name,
            modifier = Modifier.weight(1f)
        )
        ButtonIconOnly(R.drawable.copy, stringResource(R.string.to_copy), onClick = onCopy)
        ButtonDelete(onClick = onDelete)
    }
}

@DPreview
@Composable
private fun PreviewListElementToken() = ListElementToken(
    {}, {}, {}, DUserInfoPersonal.dummy
)