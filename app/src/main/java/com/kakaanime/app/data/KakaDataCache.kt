package com.kakaanime.app.data

/**
 * Small in-memory TTL cache for short-lived network/query results.
 * It intentionally has no Android/UI dependency so repositories can use it safely.
 */
class KakaDataCache<K, V>(
    private val ttlMillis: Long = 5 * 60 * 1000L,
    private val maxEntries: Int = 64,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private data class Entry<V>(val value: V, val expiresAt: Long)

    private val entries = LinkedHashMap<K, Entry<V>>(16, 0.75f, true)

    @Synchronized
    fun get(key: K): V? {
        val entry = entries[key] ?: return null
        if (entry.expiresAt <= nowMillis()) {
            entries.remove(key)
            return null
        }
        return entry.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        entries[key] = Entry(value, nowMillis() + ttlMillis)
        trim()
    }

    @Synchronized
    fun getOrPut(key: K, loader: () -> V): V {
        get(key)?.let { return it }
        return loader().also { put(key, it) }
    }

    @Synchronized
    fun remove(key: K) {
        entries.remove(key)
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    @Synchronized
    fun size(): Int = entries.size

    private fun trim() {
        while (entries.size > maxEntries) {
            entries.entries.iterator().apply {
                if (hasNext()) {
                    next()
                    remove()
                }
            }
        }
    }
}
