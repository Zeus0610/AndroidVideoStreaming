package com.zeus.videostreaming.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient

class CastViewModel(context: Context): ViewModel() {

    val mediarouterSelector: MediaRouteSelector = MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()
    val mediaRouter: MediaRouter = MediaRouter.getInstance(context)
    var route: RouteInfo? = null
    var remotePlayBackClient: RemotePlaybackClient? = null

    val mediaRouteCallback = object : MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter,
            route: RouteInfo,
            reason: Int
        ) {
            if (route.supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                this@CastViewModel.route = route

                remotePlayBackClient = RemotePlaybackClient(context, route)

                play()
            }
        }
    }

    private val itemActionCallback = object : RemotePlaybackClient.ItemActionCallback() {
        override fun onResult(
            data: Bundle,
            sessionId: String,
            sessionStatus: MediaSessionStatus?,
            itemId: String,
            itemStatus: MediaItemStatus
        ) {
            super.onResult(data, sessionId, sessionStatus, itemId, itemStatus)
        }
    }

    fun play() {
        remotePlayBackClient?.play(
            Uri.parse("http://192.168.1.64/video/index.mpd"),
            "application/dash+xml",
            null,
            0,
            null,
            itemActionCallback
        )
    }
}