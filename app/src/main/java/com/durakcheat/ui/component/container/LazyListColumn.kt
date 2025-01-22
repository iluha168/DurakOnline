package com.durakcheat.ui.component.container

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> LazyListColumn(
    list: List<T>,
    key: (T) -> Any,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    item: @Composable (LazyItemScope.(T) -> Unit)
){
    LazyColumn(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if(list.isEmpty())
            item { placeholder() }
        // Do not use else! Doing so breaks animations.
        items(list, key = key, itemContent = item)
    }
}

/** Primary use: [LazyListColumn]'s placeholder */
@Composable
fun EmptySpaceFillerText(@StringRes strID: Int){
    Text(
        text = stringResource(strID),
        modifier = Modifier
            .alpha(0.5f)
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 40.sp
    )
}