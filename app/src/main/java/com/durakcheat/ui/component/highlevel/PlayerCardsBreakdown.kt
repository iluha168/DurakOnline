package com.durakcheat.ui.component.highlevel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.durakcheat.R
import com.durakcheat.net.json.DCard
import com.durakcheat.net.json.DCardSuit
import com.durakcheat.net.json.DDeck
import com.durakcheat.ui.component.leaf.DCardDisplay
import com.durakcheat.ui.component.leaf.NamedTextCounterRow

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.playerCardsBreakdown(
    cards: List<DCard?>,
    trumpSuit: DCardSuit
) {
    val knownCards = cards.filterNotNull()
    val unknownCardsSize = cards.size - knownCards.size
    stickyHeader { NamedTextCounterRow(R.string.total, cards.size) }

    stickyHeader { NamedTextCounterRow(R.string.cards_known, knownCards.size) }
    item { DCardDisplay(cards = knownCards, trumpSuit = trumpSuit) }

    stickyHeader { NamedTextCounterRow(R.string.cards_unknown, unknownCardsSize) }
    item { DCardDisplay(cards = unknownCardsSize) }
}

@DPreview
@Composable
private fun PreviewPlayerCardsBreakdown() = LazyColumn(
    modifier = Modifier.background(Color.Gray)
) {
    playerCardsBreakdown(
        listOf(
            DDeck.DECK36.cards().shuffled().subList(0,20),
            List(10) { null }
        ).flatten(),
        DCardSuit.entries.random()
    )
}