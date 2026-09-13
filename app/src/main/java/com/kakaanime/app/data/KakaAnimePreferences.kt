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
    }
}
