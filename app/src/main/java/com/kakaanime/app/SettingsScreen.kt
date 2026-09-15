package com.kakaanime.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.ui.theme.KakaThemeMode
import com.kakaanime.app.ui.theme.LocalKakaThemeState

private val SettingsOuterPadding = 18.dp
private val SettingsSectionRadius = 22.dp
private val SettingsRowMinHeight = 56.dp
private val SettingsIconSize = 22.dp

@Composable
fun SettingsScreen(
    monetizationState: MonetizationState,
    onBack: () -> Unit,
    onPremiumClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember(context) { context.getSharedPreferences("kakaanime_settings", Context.MODE_PRIVATE) }
    val themeState = LocalKakaThemeState.current
    var defaultQuality by remember { mutableStateOf(settings.getString("default_quality", "Auto") ?: "Auto") }
    var autoNext by remember { mutableStateOf(settings.getBoolean("auto_next", true)) }
    var autoSkipIntro by remember { mutableStateOf(settings.getBoolean("auto_skip_intro", false)) }
    var autoSkipOutro by remember { mutableStateOf(settings.getBoolean("auto_skip_outro", false)) }
    var downloadQuality by remember { mutableStateOf(settings.getString("download_quality", "720p") ?: "720p") }
    var wifiOnly by remember { mutableStateOf(settings.getBoolean("wifi_only", true)) }
    var downloadNotifications by remember { mutableStateOf(settings.getBoolean("download_notifications", true)) }
    var analytics by remember { mutableStateOf(settings.getBoolean("analytics", true)) }
    var crashReports by remember { mutableStateOf(settings.getBoolean("crash_reports", true)) }
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun save(key: String, value: Any) = settings.edit().apply {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is String -> putString(key, value)
        }
    }.apply()

    val cacheSizeMb = remember { (context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } / (1024 * 1024)).coerceAtLeast(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(SettingsOuterPadding, 12.dp, SettingsOuterPadding, 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Customize your anime experience", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            SettingsSection(Icons.Outlined.Palette, "Appearance", "Choose how KakaAnime looks") {
                SettingsRow(
                    Icons.Outlined.Brightness6,
                    "Theme",
                    "Light, Dark, or follow system",
                    when (themeState.mode) {
                        KakaThemeMode.LIGHT -> "Light"
                        KakaThemeMode.DARK -> "Dark"
                        KakaThemeMode.AUTO -> "Auto"
                    }
                ) { dialog = SettingsDialog.Theme }
            }
        }

        item {
            SettingsSection(Icons.Outlined.PlayArrow, "Playback", "Video player settings") {
                SettingsRow(Icons.Outlined.OndemandVideo, "Video Player", "Configure video player options") { dialog = SettingsDialog.Player }
                SettingsRow(Icons.Outlined.HighQuality, "Default Quality", "Select default video quality", defaultQuality) { dialog = SettingsDialog.Quality }
                SettingsSwitch(Icons.Outlined.SkipNext, "Auto Next Episode", "Automatically play next episode", autoNext) { autoNext = it; save("auto_next", it) }
                SettingsSwitch(Icons.Outlined.FastForward, "Auto Skip Intro", "Automatically skip anime intros", autoSkipIntro, premium = true, enabled = monetizationState.isPremium, onLockedClick = onPremiumClick) { autoSkipIntro = it; save("auto_skip_intro", it) }
                SettingsSwitch(Icons.Outlined.FastForward, "Auto Skip Outro", "Automatically skip anime outros", autoSkipOutro, premium = true, enabled = monetizationState.isPremium, onLockedClick = onPremiumClick) { autoSkipOutro = it; save("auto_skip_outro", it) }
            }
        }

        item {
            SettingsSection(Icons.Outlined.FileDownload, "Downloads", "Manage your download settings") {
                SettingsRow(Icons.Outlined.HighQuality, "Download Quality", "Select default download quality", downloadQuality) { dialog = SettingsDialog.DownloadQuality }
                SettingsRow(Icons.Outlined.Folder, "Download Location", "Choose where to store downloads", "/KakaAnime") { message = "Download location picker akan dihubungkan ke storage manager." }
                SettingsSwitch(Icons.Outlined.Wifi, "Download over Wi-Fi only", "Only download when connected to Wi-Fi", wifiOnly) { wifiOnly = it; save("wifi_only", it) }
                SettingsRow(Icons.Outlined.Downloading, "Max Concurrent Downloads", "Limit simultaneous downloads", "3") { message = "Pengaturan jumlah download bersamaan disiapkan untuk download manager." }
                SettingsSwitch(Icons.Outlined.Notifications, "Download Notifications", "Show notification when download completes", downloadNotifications) { downloadNotifications = it; save("download_notifications", it) }
                StorageSummary(context)
                SettingsRow(Icons.Outlined.FolderOpen, "Manage Downloads", "View and manage downloaded episodes") { message = "Download manager akan terhubung di tahap Download feature." }
                SettingsRow(Icons.Outlined.Delete, "Clear All Downloads", "Remove all downloaded episodes", destructive = true) { message = "Konfirmasi penghapusan download akan muncul saat download storage sudah aktif." }
            }
        }

        item {
            SettingsSection(Icons.Outlined.Storage, "Data & Cache", "Manage storage, cache, and data usage") {
                SettingsRow(Icons.Outlined.DeleteSweep, "Clear Cache", "Remove temporary files", "${cacheSizeMb} MB", destructive = true) {
                    context.cacheDir.deleteRecursively(); message = "Cache KakaAnime berhasil dibersihkan."
                }
                SettingsRow(Icons.Outlined.Image, "Image Cache", "Cached images and thumbnails", "${cacheSizeMb} MB") { message = "Image cache memakai cache aplikasi dan akan dipisahkan dari cache umum saat image cache manager tersedia." }
                SettingsRow(Icons.Outlined.DataUsage, "Data Usage", "View mobile and Wi-Fi usage statistics") { message = "Data usage detail akan menampilkan pemakaian Mobile Data dan Wi-Fi setelah tracking jaringan diaktifkan." }
                SettingsSwitch(Icons.Outlined.Analytics, "Analytics", "Help improve KakaAnime with anonymous usage data", analytics) { analytics = it; save("analytics", it) }
                SettingsSwitch(Icons.Outlined.BugReport, "Crash Reports", "Automatically send crash reports", crashReports) { crashReports = it; save("crash_reports", it) }
            }
        }

        item {
            SettingsSection(Icons.Outlined.AccountCircle, "Account & Sync", "Protect and restore your KakaAnime data") {
                SettingsRow(Icons.Outlined.Backup, "Backup Data", "Save favorites, history, profile, and preferences") { message = "Backup data akan dibuat sebagai file KakaAnime yang bisa disimpan di perangkat." }
                SettingsRow(Icons.Outlined.Restore, "Restore Data", "Restore from a KakaAnime backup file") { message = "Restore data akan membaca file backup KakaAnime dan memulihkan data lokal." }
            }
        }
    }

    when (dialog) {
        SettingsDialog.Theme -> ThemeModeDialog(themeState, settings) { dialog = null }
        SettingsDialog.Player -> SimpleSettingsDialog("Video Player", "Pengaturan lanjutan player seperti orientasi layar, gesture seek, brightness/volume gesture, dan kontrol player akan ditempatkan di sini.") { dialog = null }
        SettingsDialog.Quality -> QualityDialog(defaultQuality, listOf("Auto", "360p", "480p", "720p", "1080p"), monetizationState.isPremium, onPremiumClick) { value -> defaultQuality = value; save("default_quality", value); dialog = null }
        SettingsDialog.DownloadQuality -> QualityDialog(downloadQuality, listOf("360p", "480p", "720p", "1080p"), monetizationState.isPremium, onPremiumClick) { value -> downloadQuality = value; save("download_quality", value); dialog = null }
        null -> Unit
    }
    message?.let { SimpleSettingsDialog("KakaAnime", it) { message = null } }
}

private enum class SettingsDialog { Theme, Player, Quality, DownloadQuality }

@Composable
private fun ThemeModeDialog(themeState: com.kakaanime.app.ui.theme.KakaThemeState, settings: android.content.SharedPreferences, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Theme") },
        text = {
            Column {
                listOf(
                    KakaThemeMode.LIGHT to "Light",
                    KakaThemeMode.DARK to "Dark",
                    KakaThemeMode.AUTO to "Auto"
                ).forEach { (mode, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            themeState.mode = mode
                            settings.edit().putString("theme_mode", mode.name).apply()
                            onClose()
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = themeState.mode == mode, onClick = {
                            themeState.mode = mode
                            settings.edit().putString("theme_mode", mode.name).apply()
                            onClose()
                        })
                        Text(label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Batal") } }
    )
}

@Composable
private fun SettingsSection(icon: ImageVector, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(SettingsSectionRadius), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .46f)) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(42.dp), CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp)) }
                }
                Spacer(Modifier.width(11.dp))
                Column { Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, value: String? = null, destructive: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = SettingsRowMinHeight).clickable(onClick = onClick).padding(horizontal = 7.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(SettingsIconSize))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (value != null) Text(value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsSwitch(icon: ImageVector, title: String, subtitle: String, checked: Boolean, premium: Boolean = false, enabled: Boolean = true, onLockedClick: () -> Unit = {}, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = SettingsRowMinHeight).padding(horizontal = 7.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f), modifier = Modifier.size(SettingsIconSize))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                if (premium) { Spacer(Modifier.width(6.dp)); Icon(Icons.Outlined.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp)) }
            }
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = { if (enabled) onCheckedChange(it) else onLockedClick() }, enabled = enabled)
    }
}

@Composable
private fun StorageSummary(context: Context) {
    val usedMb = remember { (context.filesDir.parentFile?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L) / (1024 * 1024) }
    Column(Modifier.padding(horizontal = 7.dp, vertical = 8.dp)) {
        Text("Storage", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("KakaAnime app data: ${usedMb.coerceAtLeast(0)} MB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(progress = { 0.18f }, modifier = Modifier.fillMaxWidth().height(5.dp), trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun QualityDialog(selected: String, options: List<String>, premium: Boolean, onPremiumClick: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = { onSelect(selected) },
        title = { Text("Video Quality") },
        text = {
            Column {
                options.forEach { option ->
                    val locked = option == "1080p" && !premium
                    Row(Modifier.fillMaxWidth().clickable { if (locked) onPremiumClick() else onSelect(option) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == option, onClick = { if (locked) onPremiumClick() else onSelect(option) }, enabled = !locked)
                        Text(option, modifier = Modifier.weight(1f))
                        if (locked) Text("Premium", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSelect(selected) }) { Text("Batal") } }
    )
}

@Composable
private fun SimpleSettingsDialog(title: String, message: String, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onClose) { Text("Tutup") } })
}
