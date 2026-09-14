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
import androidx.compose.material.icons.filled.Search
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
    val users = remember { listOf(SearchUserUi("Hana", "@hana"), SearchUserUi("Hanami", "@hanami"), SearchUserUi("Hana021", "@hana021"), SearchUserUi("HanaSky", "@hanasky"), SearchUserUi("Hana_chan", "@hana_chan")) }
    val filtered = users.filter { it.name.contains(query, true) || it.username.contains(query, true) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Search Users", Modifier.weight(1f).padding(start = 4.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Search, "Search", Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
        }
        OutlinedTextField(value = query, onValueChange = { query = it }, Modifier.fillMaxWidth().padding(horizontal = 18.dp), singleLine = true, placeholder = { Text("Username or ID") }, shape = RoundedCornerShape(18.dp))
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filtered.isEmpty()) item { Text("No users found", Modifier.padding(vertical = 24.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(filtered, key = { it.username }) { user ->
                Surface(Modifier.fillMaxWidth().clickable { onOpenProfile(user) }, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(user.name)
                        Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(user.name, fontWeight = FontWeight.SemiBold); Text(user.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Button(onClick = { }) { Text("Add Friend") }
                    }
                }
            }
        }
    }
}

@Composable private fun Avatar(name: String) = Surface(Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
