package com.durakcheat.ui.component.leaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.durakcheat.R
import com.durakcheat.net.packet.booleans
import com.durakcheat.ui.component.container.Rov
import com.durakcheat.ui.shadow
import com.durakcheat.ui.thenIfElse

@Composable
fun <T> SelectionRow(values: Iterable<T>, value: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier, item: @Composable (T) -> Unit){
    Rov(
        modifier = modifier
            .clip(CircleShape)
            .shadow(5.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        val primary = MaterialTheme.colorScheme.primary
        for(it in values){
            val isSelected = value == it
            Box(
                modifier = Modifier
                    .thenIfElse(isSelected,
                        Modifier
                            .shadow(10.dp, Color.Black)
                            .background(primary)
                            .zIndex(1f),
                        Modifier
                            .clickable { onSelect(it) }
                    )
                    .weight(1f)
                    .heightIn(min = 35.dp)
                ,
                contentAlignment = Alignment.Center
            ) {
                item(it)
            }
        }
    }
}

@Composable
fun <T> MultiSelectionRow(choices: Iterable<T>, selection: MutableList<T>, modifier: Modifier = Modifier, item: @Composable (T) -> Unit){
    Rov(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer)
    ){
        val primary = MaterialTheme.colorScheme.primary
        for(choice in choices){
            val isSelected = choice in selection
            Rov (
                modifier = Modifier
                    .thenIfElse(isSelected,
                        Modifier
                            .clickable { selection.remove(choice) }
                            .background(primary),
                        Modifier
                            .clickable { selection.add(choice) }
                    )
                    .weight(1f)
                    .heightIn(min = 35.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                item(choice)
                if(isSelected)
                    Icon(Icons.Default.Check, stringResource(R.string.selected_true))
                else
                    Icon(Icons.Default.Clear, stringResource(R.string.selected_false))
            }
        }
    }
}

@Composable
fun BooleanSelectionRow(b: MutableState<Boolean>, modifier: Modifier = Modifier, item: @Composable (Boolean) -> Unit){
    SelectionRow(values = booleans, value = b.value, onSelect = { b.value = it }, modifier = modifier, item = item)
}