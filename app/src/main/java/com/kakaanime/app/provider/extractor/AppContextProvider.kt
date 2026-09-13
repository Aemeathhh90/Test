package com.kakaanime.app.provider.extractor

import android.content.Context

object AppContextProvider {
    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun requireContext(): Context = requireNotNull(applicationContext) {
        "AppContextProvider is not initialized"
    }
}
