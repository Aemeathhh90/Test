package com.kakaanime.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class KakaAnimePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadFavoriteTitles(): Set<String> = loadStringSet(KEY_FAVORITES)

    fun saveFavoriteTitles(titles: Set<String>) {
        saveStringSet(KEY_FAVORITES, titles)
    }

    fun loadWatchedEpisodes(): Map<String, Int> {
        val raw = prefs.getString(KEY_WATCHED_EPISODES, null) ?: return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { title -> put(title, json.optInt(title, 0)) }
            }.filterValues { it > 0 }
        }.getOrDefault(emptyMap())
    }

    fun saveWatchedEpisodes(episodes: Map<String, Int>) {
        val json = JSONObject()
        episodes.forEach { (title, episode) -> json.put(title, episode) }
        prefs.edit().putString(KEY_WATCHED_EPISODES, json.toString()).apply()
    }

    fun loadDiamonds(): Int = prefs.getInt(KEY_DIAMONDS, 0)

    fun saveDiamonds(value: Int) {
        prefs.edit().putInt(KEY_DIAMONDS, value.coerceAtLeast(0)).apply()
    }

    fun loadPremium(): Boolean = prefs.getBoolean(KEY_PREMIUM, false)

    fun savePremium(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM, value).apply()
    }

    fun loadProfileName(): String = prefs.getString(KEY_PROFILE_NAME, "KakaAnime User") ?: "KakaAnime User"

    fun saveProfileName(value: String) {
        prefs.edit().putString(KEY_PROFILE_NAME, value.trim().ifBlank { "KakaAnime User" }).apply()
    }

    fun loadProfileBio(): String = prefs.getString(KEY_PROFILE_BIO, "Anime selalu bersama kamu.") ?: "Anime selalu bersama kamu."

    fun saveProfileBio(value: String) {
        prefs.edit().putString(KEY_PROFILE_BIO, value.trim()).apply()
    }

    fun loadProfileAvatarIndex(): Int = prefs.getInt(KEY_PROFILE_AVATAR, 0).coerceIn(0, 3)

    fun saveProfileAvatarIndex(value: Int) {
        prefs.edit().putInt(KEY_PROFILE_AVATAR, value.coerceIn(0, 3)).apply()
    }

    fun loadProfileBannerIndex(): Int = prefs.getInt(KEY_PROFILE_BANNER, 0).coerceIn(0, 3)

    fun saveProfileBannerIndex(value: Int) {
        prefs.edit().putInt(KEY_PROFILE_BANNER, value.coerceIn(0, 3)).apply()
    }

    fun loadDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    fun saveDarkMode(value: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    }

    fun loadAccentName(): String = prefs.getString(KEY_ACCENT, "Blue") ?: "Blue"

    fun saveAccentName(value: String) {
        prefs.edit().putString(KEY_ACCENT, value).apply()
    }

    private fun loadStringSet(key: String): Set<String> {
        val raw = prefs.getString(key, null) ?: return emptySet()
        return runCatching {
            val array = JSONArray(raw)
            buildSet { for (index in 0 until array.length()) add(array.getString(index)) }
        }.getOrDefault(emptySet())
    }

    private fun saveStringSet(key: String, values: Set<String>) {
        val array = JSONArray()
        values.sorted().forEach(array::put)
        prefs.edit().putString(key, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "kakaanime_user_state"
        private const val KEY_FAVORITES = "favorite_titles"
        private const val KEY_WATCHED_EPISODES = "watched_episodes"
        private const val KEY_DIAMONDS = "diamonds"
        private const val KEY_PREMIUM = "premium"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_BIO = "profile_bio"
        private const val KEY_PROFILE_AVATAR = "profile_avatar"
        private const val KEY_PROFILE_BANNER = "profile_banner"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ACCENT = "accent"
    }
}
