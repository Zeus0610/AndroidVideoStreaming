package com.zeus.videostreaming.viewmodel

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {

    private var _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }
    }

    @OptIn(UnstableApi::class)
    fun initializePlayer(uri: String, context: Context) {
        val player = ExoPlayer.Builder(context.applicationContext)
            .build()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.addListener(playerListener)
        player.prepare()


        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(shouldEnterPipMode = isPlaying)
            }
        })

        _state.value = _state.value.copy(player = player)
        _state.value = _state.value.copy(shouldEnterPipMode = true)
    }

    fun playPause() {
        if (state.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        _state.value.player?.play()
    }

    private fun pause() {
        _state.value.player?.pause()
    }

    fun releasePlayer() {
        _state.value.player?.release()
        _state.value = _state.value.copy(
            player = null,
            shouldEnterPipMode = false
        )
    }

    fun setFullScreenState(isOnFullScreen: Boolean) {
        _state.value = _state.value.copy(
            isOnFullScreen = isOnFullScreen
        )
    }

    fun setPipMode(isOnPipMode: Boolean) {
        _state.value = _state.value.copy(
            isOnPipMode = isOnPipMode
        )
    }
}