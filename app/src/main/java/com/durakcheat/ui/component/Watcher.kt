package com.durakcheat.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T> Watcher(getValue: () -> T, onChange: (new: T, old: T) -> Unit, callOnInit: Boolean = true){
    var old by remember {
        val init = getValue()
        if(callOnInit) onChange(init, init)
        mutableStateOf(init)
    }
    val new = getValue()
    if(old != new){
        onChange(new, old)
        old = new
    }
}

@Composable
inline fun <T> Watcher(noinline getValue: () -> T, crossinline onChange: (new: T) -> Unit, callOnInit: Boolean = true){
    Watcher(getValue, { new, _ ->
        onChange(new)
    }, callOnInit)
}

@Composable
inline fun <T> Watcher(noinline getValue: () -> T, crossinline onChange: () -> Unit, callOnInit: Boolean = true){
    Watcher(getValue, { _, _ ->
        onChange()
    }, callOnInit)
}