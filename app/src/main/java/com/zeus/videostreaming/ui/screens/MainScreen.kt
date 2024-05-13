package com.zeus.videostreaming.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zeus.videostreaming.BuildConfig
import com.zeus.videostreaming.ui.components.CastButton
import com.zeus.videostreaming.utils.findActivity
import com.zeus.videostreaming.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PlayerViewModel,
    onPipClick: () -> Unit,
    onFullScreen: () -> Unit
) {
    val inPipMode = rememberIsInPipMode()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PlayerLifeCycle(
        initialize = {
            viewModel.initializePlayer(
                BuildConfig.BASE_URL,
                context
            )
        },
        release = viewModel::releasePlayer
    )

    if (inPipMode || state.value.isOnFullScreen) {
        StreamingVideoPlayer(
            modifier = Modifier
                .fillMaxSize(0.9f),
            player = state.value.player,
            isPlaying = state.value.isPlaying,
            isOnPipMode = state.value.isOnPipMode,
            onPipClick = onPipClick,
            onFullScreenClick = onFullScreen,
            onTimeChange = { /*todo*/ },
            onPlayPause = viewModel::playPause
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Halo Video Streaming")
                    }
                )
            }
        ) {
            Card(
                modifier = Modifier.padding(it)
            ) {
                Column {
                    StreamingVideoPlayer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        player = state.value.player,
                        isPlaying = state.value.isPlaying,
                        isOnPipMode = state.value.isOnPipMode,
                        onPlayPause = viewModel::playPause,
                        onTimeChange = { /*todo*/ },
                        onPipClick = onPipClick,
                        onFullScreenClick = onFullScreen
                    )
                    CastButton(modifier = Modifier.size(25.dp))
                }
            }
        }
    }
}

@Composable
fun rememberIsInPipMode(): Boolean {
    val activity = LocalContext.current.findActivity()
    var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }
    DisposableEffect(activity) {
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            pipMode = info.isInPictureInPictureMode
        }
        activity.addOnPictureInPictureModeChangedListener(
            observer
        )
        onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
    }
    return pipMode
}

@Composable
private fun PlayerLifeCycle(
    initialize: () -> Unit,
    release: () -> Unit
) {
    val currentOnInitializePlayer by rememberUpdatedState(newValue = initialize)
    val currentOnReleasePlayer by rememberUpdatedState(newValue = release)

    /**
     * Android API level 24 and higher supports multiple windows. As your app can be visible, but
     * not active in split window mode, you need to initialize the player in onStart
     */
    LifecycleStartEffect(true) {
        currentOnInitializePlayer()
        onStopOrDispose {
            currentOnReleasePlayer()
        }
    }
}