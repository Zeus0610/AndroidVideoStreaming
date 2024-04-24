package com.zeus.videostreaming.viewmodel

import androidx.media3.common.Player

data class PlayerState(
    val player: Player? = null,
    val isPlaying: Boolean = false,
    val isOnFullScreen: Boolean = false,
    val shouldEnterPipMode: Boolean = false
)
