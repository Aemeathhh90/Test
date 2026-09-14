package com.kakaanime.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SearchUserUi(val name: String, val username: String)

@Composable
fun SearchUsersScreen(onBack: () -> Unit = {}, onOpenProfile: (SearchUserUi) -> Unit = {}) {
    var query by remember { mutableStateOf("") }
    val users = listOf(
        SearchUserUi("Hana", "@hana"),
        SearchUserUi("Hanami", "@hanami"),
        SearchUserUi("Hana021", "@hana021"),
        SearchUserUi("HanaSky", "@hanasky"),
        SearchUserUi("Hana_chan", "@hana_chan"),
    )
    val filtered = users.filter { it.name.contains(query, true) || it.username.contains(query, true) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Search Users", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            singleLine = true,
            placeholder = { Text("Search users (username or ID)") },
            shape = RoundedCornerShape(18.dp),
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filtered, key = { it.username }) { user ->
                Surface(Modifier.fillMaxWidth().clickable { onOpenProfile(user) }, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(user.name.take(1), modifier = Modifier.padding(top = 11.dp), fontWeight = FontWeight.Bold) }
                        }
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.SemiBold)
                            Text(user.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { }) { Text("Add Friend") }
                    }
                }
            }
        }
    }
}
