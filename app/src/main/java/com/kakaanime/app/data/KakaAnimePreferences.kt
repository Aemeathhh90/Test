package com.kakaanime.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class KakaAnimePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    fun loadFavoriteTitles(): Set<String> = loadStringSet(KEY_FAVORITES)
    fun saveFavoriteTitles(titles: Set<String>) { saveStringSet(KEY_FAVORITES, titles) }
    fun loadWatchedEpisodes(): Map<String, Int> {
        val raw = prefs.getString(KEY_WATCHED_EPISODES, null) ?: return emptyMap()
        return runCatching { val json = JSONObject(raw); buildMap { json.keys().forEach { title -> put(title, json.optInt(title, 0)) } }.filterValues { it > 0 } }.getOrDefault(emptyMap())
    }
    fun saveWatchedEpisodes(episodes: Map<String, Int>) { val json = JSONObject(); episodes.forEach { (title, episode) -> json.put(title, episode) }; prefs.edit().putString(KEY_WATCHED_EPISODES, json.toString()).apply() }
    fun loadWatchHistory(): List<WatchHistoryEntry> {
        val raw = prefs.getString(KEY_WATCH_HISTORY, null) ?: return emptyList()
        return runCatching { val array = JSONArray(raw); buildList { for (i in 0 until array.length()) { val item = array.optJSONObject(i) ?: continue; val title = item.optString("title").trim(); val episode = item.optInt("episode", 0); if (title.isBlank() || episode <= 0) continue; add(WatchHistoryEntry(title, episode, item.optString("episodeTitle").takeIf { it.isNotBlank() }, item.optString("episodeThumbnailUrl").takeIf { it.isNotBlank() }, item.optLong("watchedAt", 0L), item.optLong("durationMs", 0L))) } }.sortedByDescending { it.watchedAt } }.getOrDefault(emptyList())
    }
    fun recordWatchedEpisode(title: String, episode: Int, episodeTitle: String? = null, episodeThumbnailUrl: String? = null, durationMs: Long = 0L) {
        val current = loadWatchHistory().toMutableList(); val now = System.currentTimeMillis(); val index = current.indexOfFirst { it.title == title && it.episode == episode }; val old = current.getOrNull(index)
        val entry = WatchHistoryEntry(title, episode, episodeTitle ?: old?.episodeTitle, episodeThumbnailUrl ?: old?.episodeThumbnailUrl, now, if (durationMs > 0L) durationMs else old?.durationMs ?: 0L)
        if (index >= 0) current[index] = entry else current.add(entry); saveWatchHistory(current.take(MAX_WATCH_HISTORY_ENTRIES))
    }
    fun deleteWatchHistoryEntry(title: String, episode: Int) {
        saveWatchHistory(loadWatchHistory().filterNot { it.title == title && it.episode == episode })
    }
    fun clearWatchHistory() { saveWatchHistory(emptyList()) }
    private fun saveWatchHistory(entries: List<WatchHistoryEntry>) { val array = JSONArray(); entries.forEach { e -> array.put(JSONObject().put("title", e.title).put("episode", e.episode).put("episodeTitle", e.episodeTitle.orEmpty()).put("episodeThumbnailUrl", e.episodeThumbnailUrl.orEmpty()).put("watchedAt", e.watchedAt).put("durationMs", e.durationMs)) }; prefs.edit().putString(KEY_WATCH_HISTORY, array.toString()).apply() }
    fun loadDiamonds(): Int = prefs.getInt(KEY_DIAMONDS, 0)
    fun saveDiamonds(value: Int) { prefs.edit().putInt(KEY_DIAMONDS, value.coerceAtLeast(0)).apply() }
    fun loadPremium(): Boolean = prefs.getBoolean(KEY_PREMIUM, false)
    fun savePremium(value: Boolean) { prefs.edit().putBoolean(KEY_PREMIUM, value).apply() }
    fun loadProfileName(): String = prefs.getString(KEY_PROFILE_NAME, "KakaAnime User") ?: "KakaAnime User"
    fun saveProfileName(value: String) { prefs.edit().putString(KEY_PROFILE_NAME, value.trim().ifBlank { "KakaAnime User" }).apply() }
    fun loadProfileBio(): String = prefs.getString(KEY_PROFILE_BIO, "Anime selalu bersama kamu.") ?: "Anime selalu bersama kamu."
    fun saveProfileBio(value: String) { prefs.edit().putString(KEY_PROFILE_BIO, value.trim()).apply() }
    fun loadProfileAvatarIndex(): Int = prefs.getInt(KEY_PROFILE_AVATAR, 0).coerceIn(0, 3)
    fun saveProfileAvatarIndex(value: Int) { prefs.edit().putInt(KEY_PROFILE_AVATAR, value.coerceIn(0, 3)).apply() }
    fun loadProfileBannerIndex(): Int = prefs.getInt(KEY_PROFILE_BANNER, 0).coerceIn(0, 3)
    fun saveProfileBannerIndex(value: Int) { prefs.edit().putInt(KEY_PROFILE_BANNER, value.coerceIn(0, 3)).apply() }
    fun loadProfilePhotoUri(): String? = prefs.getString(KEY_PROFILE_PHOTO_URI, null)
    fun saveProfilePhotoUri(value: String?) { saveNullableString(KEY_PROFILE_PHOTO_URI, value) }
    fun loadProfileBannerUri(): String? = prefs.getString(KEY_PROFILE_BANNER_URI, null)
    fun saveProfileBannerUri(value: String?) { saveNullableString(KEY_PROFILE_BANNER_URI, value) }
    fun loadPremiumBannerUri(): String? = prefs.getString(KEY_PREMIUM_BANNER_URI, null)
    fun savePremiumBannerUri(value: String?) { saveNullableString(KEY_PREMIUM_BANNER_URI, value) }
    fun loadAnimatedProfileUri(): String? = prefs.getString(KEY_ANIMATED_PROFILE_URI, null)
    fun saveAnimatedProfileUri(value: String?) { saveNullableString(KEY_ANIMATED_PROFILE_URI, value) }
    private fun saveNullableString(key: String, value: String?) { prefs.edit().apply { if (value.isNullOrBlank()) remove(key) else putString(key, value) }.apply() }
    fun loadDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)
    fun saveDarkMode(value: Boolean) { prefs.edit().putBoolean(KEY_DARK_MODE, value).apply() }
    fun loadAccentName(): String = prefs.getString(KEY_ACCENT, "Blue") ?: "Blue"
    fun saveAccentName(value: String) { prefs.edit().putString(KEY_ACCENT, value).apply() }
    private fun loadStringSet(key: String): Set<String> { val raw = prefs.getString(key, null) ?: return emptySet(); return runCatching { val array = JSONArray(raw); buildSet { for (i in 0 until array.length()) add(array.getString(i)) } }.getOrDefault(emptySet()) }
    private fun saveStringSet(key: String, values: Set<String>) { val array = JSONArray(); values.sorted().forEach(array::put); prefs.edit().putString(key, array.toString()).apply() }
    companion object { private const val PREFS_NAME = "kakaanime_user_state"; private const val KEY_FAVORITES = "favorite_titles"; private const val KEY_WATCHED_EPISODES = "watched_episodes"; private const val KEY_WATCH_HISTORY = "watch_history"; private const val KEY_DIAMONDS = "diamonds"; private const val KEY_PREMIUM = "premium"; private const val KEY_PROFILE_NAME = "profile_name"; private const val KEY_PROFILE_BIO = "profile_bio"; private const val KEY_PROFILE_AVATAR = "profile_avatar"; private const val KEY_PROFILE_BANNER = "profile_banner"; private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"; private const val KEY_PROFILE_BANNER_URI = "profile_banner_uri"; private const val KEY_PREMIUM_BANNER_URI = "premium_banner_uri"; private const val KEY_ANIMATED_PROFILE_URI = "animated_profile_uri"; private const val KEY_DARK_MODE = "dark_mode"; private const val KEY_ACCENT = "accent"; private const val MAX_WATCH_HISTORY_ENTRIES = 2000 }
}

data class WatchHistoryEntry(val title: String, val episode: Int, val episodeTitle: String?, val episodeThumbnailUrl: String?, val watchedAt: Long, val durationMs: Long)
