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
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.zeus.videostreaming.ui.screens.MainScreen
import com.zeus.videostreaming.ui.theme.VideoStreamingTheme
import com.zeus.videostreaming.utils.ACTION_PLAYER_CONTROLS
import com.zeus.videostreaming.utils.CONTROL_TYPE_PLAY_PAUSE
import com.zeus.videostreaming.utils.EXTRA_CONTROL_TYPE
import com.zeus.videostreaming.utils.REQUEST_PAUSE
import com.zeus.videostreaming.utils.REQUEST_PLAY
import com.zeus.videostreaming.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewmodel by viewModels<PlayerViewModel>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_PLAYER_CONTROLS) {
                return
            }

            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_TYPE_PLAY_PAUSE -> {
                    playerViewmodel.playPause()
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

                    LaunchedEffect(key1 = playerState.value.isOnPipMode) {
                        if (playerState.value.isOnPipMode) {
                            enterPictureInPictureMode(updatePipParams(playerState.value.isPlaying))
                        }
                        playerViewmodel.setFullScreenState(false)
                    }

                    LaunchedEffect(key1 = playerState.value.isPlaying) {
                        if (playerState.value.isOnPipMode) {
                            setPictureInPictureParams(updatePipParams(playerState.value.isPlaying))
                        }
                    }

                    MainScreen(
                        playerViewmodel,
                        onPipClick = { playerViewmodel.setPipMode(true) },
                        onFullScreen = ::toggleFullScreen
                    )
                }
            }
        }

        registerReceiver(
            broadcastReceiver,
            IntentFilter(ACTION_PLAYER_CONTROLS),
            RECEIVER_EXPORTED
        )
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        playerViewmodel.setPipMode(isInPictureInPictureMode)
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
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
                            CONTROL_TYPE_PLAY_PAUSE
                        )
                    } else {
                        createRemoteAction(
                            android.R.drawable.ic_media_play,
                            R.string.play,
                            REQUEST_PLAY,
                            CONTROL_TYPE_PLAY_PAUSE
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

    private fun toggleFullScreen() {
        val isOnFullScreen = playerViewmodel.state.value.isOnFullScreen

        if (!isOnFullScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars())
                    hide(WindowInsets.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }

        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            WindowCompat.setDecorFitsSystemWindows(window, true)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.insetsController?.apply {
                    show(WindowInsets.Type.statusBars())
                    show(WindowInsets.Type.navigationBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            }
        }
        playerViewmodel.setFullScreenState(isOnFullScreen.not())

    }
}
