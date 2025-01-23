package com.durakcheat.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.durakcheat.MainActivity
import com.durakcheat.R
import com.durakcheat.net.packet.DUserInfoPersonal
import com.durakcheat.ui.component.container.EmptySpaceFillerText
import com.durakcheat.ui.component.container.LazyListColumn
import com.durakcheat.ui.component.highlevel.ListElementToken
import com.durakcheat.ui.component.leaf.ButtonIconOnly
import com.durakcheat.ui.component.leaf.InputLineField
import com.durakcheat.ui.dialog.confirmationDialog
import kotlinx.coroutines.launch

@Composable
fun ChooseAccountScreen(activity: MainActivity, tokens: MutableList<String>, onChoice: (String) -> Unit){
    val users = remember { mutableStateMapOf<String, DUserInfoPersonal>() }
    val clipboard = LocalClipboardManager.current
    val tokenDeleteDlgOpener = confirmationDialog(
        titleText = R.string.delete_confirm,
        bodyText = stringResource(R.string.dlg_body_token_delete),
        confirmBtnText = R.string.delete,
        onConfirm = { token: String ->
            tokens.remove(token)
            users.remove(token)
        }
    )

    LaunchedEffect(Unit) {
        val initUsers = activity.client.fetchUsersByTokens(tokens)
        users.clear()
        for (pair in initUsers)
            users[pair.key] = pair.value
    }
    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InputLineField(
            placeholder = stringResource(R.string.placeholder_add_token),
            modifier = Modifier.fillMaxWidth(),
            autoClear = true,
            onEnter = { token -> coroutineScope.launch {
                for (pair in activity.client.fetchUsersByTokens(listOf(token))) {
                    users[pair.key] = pair.value
                    tokens.add(pair.key)
                }
            } }
        )
        LazyListColumn(
            list = users.entries.toList(), key = {it.key},
            placeholder = { EmptySpaceFillerText(R.string.no_accounts) },
            modifier = Modifier.weight(1f)
        ) {
            ListElementToken(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                onClick = { onChoice(it.key) },
                onCopy = { clipboard.setText(AnnotatedString(it.key)) },
                onDelete = { tokenDeleteDlgOpener(it.key) },
                user = it.value
            )
        }
        HorizontalDivider()
        Row {
            ButtonIconOnly(
                R.drawable.palette,
                stringResource(R.string.screen_palette),
                onClick = { activity.nav.navigate("palette") }
            )
        }
    }
}