package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MessageConversationUi(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unread: Int = 0,
)

data class ChatMessageUi(
    val text: String,
    val mine: Boolean,
)

@Composable
fun MessagesScreen(onBack: () -> Unit = {}) {
    var selectedConversation by remember { mutableStateOf<MessageConversationUi?>(null) }

    if (selectedConversation != null) {
        ChatDetailScreen(
            conversation = selectedConversation!!,
            onBack = { selectedConversation = null },
        )
    } else {
        MessagesListScreen(onBack = onBack, onOpenConversation = { selectedConversation = it })
    }
}

@Composable
private fun MessagesListScreen(
    onBack: () -> Unit,
    onOpenConversation: (MessageConversationUi) -> Unit,
) {
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val conversations = remember {
        listOf(
            MessageConversationUi("Kael", "Bro, episode barunya udah rilis?", "12:24", 2),
            MessageConversationUi("Hana", "Yuk nonton bareng nanti malam!", "11:03", 1),
            MessageConversationUi("Rynn", "Gila sih, episode kemarin keren.", "Yesterday"),
        )
    }
    val filtered = conversations.filter { it.name.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text("Messages", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Your conversations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { searching = !searching }) {
                Icon(Icons.Default.Search, contentDescription = "Search messages")
            }
        }

        if (searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                singleLine = true,
                label = { Text("Search") },
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            EmptyMessagesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.name }) { conversation ->
                    ConversationCard(conversation, onClick = { onOpenConversation(conversation) })
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(conversation: MessageConversationUi, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(conversation.name)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(conversation.name, fontWeight = FontWeight.SemiBold)
                Text(
                    conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(conversation.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (conversation.unread > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            conversation.unread.toString(),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("No conversations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Your messages will appear here when you start a conversation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChatDetailScreen(conversation: MessageConversationUi, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessageUi("Nonton malam?", false),
                ChatMessageUi("Gas, jam 9.", true),
                ChatMessageUi("Oke.", false),
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text(conversation.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Direct message", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Today",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().imePadding().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
            )
            IconButton(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        messages = messages + ChatMessageUi(text, true)
                        draft = ""
                    }
                },
                enabled = draft.trim().isNotEmpty(),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (message.mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (message.mine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
