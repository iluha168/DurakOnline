package com.durakcheat.ui.component.leaf

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun InputLineField(placeholder: String, modifier: Modifier = Modifier, autoClear: Boolean = false, onEnter: suspend (String) -> Unit){
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    TextField(
        value = input,
        onValueChange = {
            input = it
            if(it.endsWith('\n')){
                input = input.trim()
                if(input.isNotEmpty()) {
                    val copy = input
                    coroutineScope.launch {
                        onEnter(copy)
                    }
                    if(autoClear) input = ""
                }
            }
        },
        placeholder = { Text(placeholder) },
        modifier = modifier,
    )
}