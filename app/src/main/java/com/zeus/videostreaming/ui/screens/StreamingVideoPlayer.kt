package com.zeus.videostreaming.ui.screens

import android.app.PictureInPictureParams
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toRect
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.zeus.videostreaming.ui.components.PlayerControls
import com.zeus.videostreaming.utils.findActivity

@OptIn(UnstableApi::class)
@Composable
fun StreamingVideoPlayer(
    modifier: Modifier  = Modifier,
    player: Player? = null,
    isPlaying: Boolean = false,
    isOnPipMode: Boolean = false,
    onPlayPause: () -> Unit = {},
    onTimeChange: (Float) -> Unit = {},
    onPipClick: () -> Unit = {},
    onFullScreenClick: () -> Unit = {}
) {
    KeepScreenOn()
    val context = LocalContext.current
    val pipModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        val builder = PictureInPictureParams.Builder()
        val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
        builder.setSourceRectHint(sourceRect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
        }
        context.findActivity().setPictureInPictureParams(builder.build())
    }

    PlayerControls(
        modifier = modifier,
        isPlaying = isPlaying,
        videoTime = 0.5f,
        isOnPipMode = isOnPipMode,
        onPlayPause = onPlayPause,
        onTimeChange = onTimeChange,
        onPipClick = onPipClick,
        onFullScreenClick = onFullScreenClick
    ) {
        AndroidView(
            modifier = pipModifier
                .fillMaxWidth()
                .focusable(),
            factory = { PlayerView(it) },
            update = { playerView ->
                playerView.player = player
                playerView.useController = false
            }
        )
    }

}

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
