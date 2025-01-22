package com.durakcheat.ui.component.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val CardRounding = 10.dp

@Composable
fun TitleText(text: String, modifier: Modifier = Modifier){
    Text(
        text = text,
        fontSize = 30.sp,
        lineHeight = 30.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(10.dp)
    )
}

@Composable
fun ColumnDialog(title: String, closer: () -> Unit, content: @Composable ColumnScope.() -> Unit){
    Dialog(
        onDismissRequest = closer,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            shape = RoundedCornerShape(CardRounding),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ){
            TitleText(title, Modifier.fillMaxWidth())
            Column(
                modifier = Modifier.padding(CardRounding),
                content = content
            )
        }
    }
}

@Composable
fun <T : Any> columnDialog(title: String, content: @Composable ColumnScope.(T, closer: () -> Unit) -> Unit): (T) -> Unit {
    var context by remember { mutableStateOf<T?>(null) }
    val closer = {context = null}
    if(context != null)
        ColumnDialog(title, closer){
            if(context != null)
                content(context!!, closer)
        }
    return {context = it}
}

fun <T> ((T)->Unit).bind(arg: T) = ({ this(arg) })

@Composable
inline fun <T : Any> columnDialog(title: String, crossinline content: @Composable ColumnScope.(T) -> Unit)
        = columnDialog<T>(title){ v,_ -> content(v) }

@Composable
inline fun columnDialog(title: String, crossinline content: @Composable ColumnScope.() -> Unit)
        = columnDialog<Unit>(title){ _,_ -> content() }.bind(Unit)

@Composable
fun <T : Any> lazyColumnDialog(title: String, content: LazyListScope.(T, closer: () -> Unit) -> Unit): (T) -> Unit {
    return columnDialog(title) { context, closer ->
        LazyColumn {
            content(context, closer)
        }
    }
}

@Composable
inline fun <T : Any> lazyColumnDialog(title: String, crossinline content: LazyListScope.(T) -> Unit)
        = lazyColumnDialog<T>(title){ v,_ -> content(v) }

@Composable
inline fun lazyColumnDialog(title: String, crossinline content: LazyListScope.() -> Unit)
        = lazyColumnDialog<Unit>(title){ _,_ -> content() }.bind(Unit)