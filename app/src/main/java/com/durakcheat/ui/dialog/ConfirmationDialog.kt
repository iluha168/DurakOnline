package com.durakcheat.ui.dialog

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.durakcheat.R
import com.durakcheat.ui.component.leaf.ButtonTextOnly

@Composable
fun <T : Any> confirmationDialog(
    @StringRes confirmBtnText: Int? = null,
    @StringRes titleText: Int,
    bodyText: String,
    onConfirm: (T) -> Unit
): (T) -> Unit {
    var value by remember { mutableStateOf<T?>(null) }
    val closer = { value = null }
    if(value != null)
        AlertDialog(
            confirmButton = {
                ButtonTextOnly(stringResource(confirmBtnText ?: R.string.ok)) {
                    onConfirm(value!!)
                    closer()
                }
            },
            dismissButton = confirmBtnText?.run { @Composable {
                ButtonTextOnly(text = stringResource(R.string.cancel), onClick = closer)
            }},
            onDismissRequest = closer,
            title = { Text(stringResource(titleText)) },
            text = { Text(bodyText) },
        )
    return { value = it }
}