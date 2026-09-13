package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream

class BrowserMediaResolver {
    suspend fun resolve(
        url: String,
        referer: String? = null
    ): List<ProviderStream> {
        val context = runCatching { AppContextProvider.requireContext() }.getOrNull()
            ?: return emptyList()
        return WebViewStreamResolver(context).resolve(url, referer)
    }
}
