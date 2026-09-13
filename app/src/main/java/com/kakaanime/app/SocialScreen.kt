package com.kakaanime.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Social V1 foundation destination; full interactions remain deferred. */
@Composable
fun SocialScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Social", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Friends dan Watch Together. Fitur sosial lengkap menyusul di V2.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        item { SocialFoundationCard("Friends", "Teman, requests, pencarian user, profile, block/report") }
        item { SocialFoundationCard("Watch Together", "Create, Join, Active Rooms, invite friends, dan sinkronisasi playback") }
        item { SocialFoundationCard("Activity & Presence", "Fondasi aktivitas teman dan status presence") }
        item { SocialFoundationCard("Moderation & Privacy", "Fondasi privasi, keamanan, dan interaksi text-only") }
    }
}

@Composable
private fun SocialFoundationCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
