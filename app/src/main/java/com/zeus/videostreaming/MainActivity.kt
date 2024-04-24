package com.zeus.videostreaming

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.zeus.videostreaming.ui.screens.MainScreen
import com.zeus.videostreaming.ui.theme.VideoStreamingTheme
import com.zeus.videostreaming.utils.ACTION_PLAYER_CONTROLS
import com.zeus.videostreaming.utils.CONTROL_TYPE_PAUSE
import com.zeus.videostreaming.utils.CONTROL_TYPE_PLAY
import com.zeus.videostreaming.utils.EXTRA_CONTROL_TYPE
import com.zeus.videostreaming.utils.REQUEST_PAUSE
import com.zeus.videostreaming.utils.REQUEST_PLAY
import com.zeus.videostreaming.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val playerViewmodel by viewModels<PlayerViewModel>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_PLAYER_CONTROLS) {
                return
            }

            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_TYPE_PLAY -> {
                    playerViewmodel.play()
                }

                CONTROL_TYPE_PAUSE -> {
                    playerViewmodel.pause()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoStreamingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val playerState = playerViewmodel.state.collectAsState()
                    MainScreen(
                        playerViewmodel,
                        onPipClick = {
                            enterPictureInPictureMode(updatePipParams(playerState.value.isPlaying))
                            playerViewmodel.setFullScreenState(false)
                        },
                        onFullScreen = ::fullScreen
                    )
                }
            }
        }

        lifecycleScope.launch {
            playerViewmodel.state.collect {
                setPictureInPictureParams(updatePipParams(it.isPlaying))
            }
        }

        registerReceiver(
            broadcastReceiver,
            IntentFilter(ACTION_PLAYER_CONTROLS),
            RECEIVER_EXPORTED
        )
    }

    private fun updatePipParams(isPlaying: Boolean): PictureInPictureParams {
        val pipParams = PictureInPictureParams.Builder()
            .setActions(
                listOf(
                    if (isPlaying) {
                        createRemoteAction(
                            android.R.drawable.ic_media_pause,
                            R.string.pause,
                            REQUEST_PAUSE,
                            CONTROL_TYPE_PAUSE
                        )
                    } else {
                        createRemoteAction(
                            android.R.drawable.ic_media_play,
                            R.string.play,
                            REQUEST_PLAY,
                            CONTROL_TYPE_PLAY
                        )
                    }
                )
            )
            .setAspectRatio(Rational(16, 9))
            .build()

        return pipParams
    }

    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        requestCode: Int,
        controlType: Int
    ): RemoteAction {
        return RemoteAction(
            Icon.createWithResource(this, iconResId),
            getString(titleResId),
            getString(titleResId),
            PendingIntent.getBroadcast(
                this,
                requestCode,
                Intent(ACTION_PLAYER_CONTROLS)
                    .putExtra(EXTRA_CONTROL_TYPE, controlType),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    private fun fullScreen() {
        playerViewmodel.setFullScreenState(true)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}
