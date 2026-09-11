package com.kakaanime.app.player.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PlayerControlsState {

    var isVisible by mutableStateOf(true)
        private set

    private var resetCounter by mutableIntStateOf(0)

    val resetKey: Int
        get() = resetCounter

    fun show() {
        isVisible = true
        resetCounter++
    }

    fun hide() {
        isVisible = false
    }

    fun toggle() {
        if (isVisible) {
            hide()
        } else {
            show()
        }
    }
}
