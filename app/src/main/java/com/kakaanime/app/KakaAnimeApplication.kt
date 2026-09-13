package com.kakaanime.app

import android.app.Application
import com.kakaanime.app.provider.extractor.AppContextProvider

class KakaAnimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextProvider.initialize(this)
    }
}
