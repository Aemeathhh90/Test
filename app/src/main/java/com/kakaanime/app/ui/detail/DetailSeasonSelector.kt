package com.kakaanime.app.ui.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Small presentation-only season selector for Detail V1. */
@Composable
fun DetailSeasonSelector(
    seasons: List<SeasonUi>,
    selectedSeason: SeasonUi?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSeasonSelected: (SeasonUi) -> Unit,
) {
    if (seasons.size <= 1) return

    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(selectedSeason?.label ?: "Pilih Season", modifier = Modifier.weight(1f))
            Text("▼")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            seasons.forEach { season ->
                DropdownMenuItem(
                    text = { Text(season.label) },
                    onClick = {
                        onExpandedChange(false)
                        onSeasonSelected(season)
                    },
                )
            }
        }
    }
}
