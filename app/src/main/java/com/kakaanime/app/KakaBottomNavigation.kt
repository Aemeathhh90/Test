package com.kakaanime.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KakaBottomNavigation(selectedTab: Any, onTabSelected: (Any) -> Unit) {
    val labels = listOf("Home", "Calendar", "Social", "Library", "Profile")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.CalendarMonth, Icons.Outlined.Groups, Icons.Outlined.CollectionsBookmark, Icons.Outlined.Person)
    Surface(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
        tonalElevation = 6.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.indices.forEach { index ->
                val selected = selectedTab.toString().substringAfterLast('.').equals(labels[index].uppercase(), true)
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onTabSelected(selectedTabFromIndex(index, selectedTab)) }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        icons[index],
                        labels[index],
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        labels[index],
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun selectedTabFromIndex(index: Int, current: Any): Any = when (index) {
    0 -> current.javaClass.enumConstants?.getOrNull(0)
    1 -> current.javaClass.enumConstants?.getOrNull(1)
    2 -> current.javaClass.enumConstants?.getOrNull(2)
    3 -> current.javaClass.enumConstants?.getOrNull(3)
    else -> current.javaClass.enumConstants?.getOrNull(4)
} ?: current
