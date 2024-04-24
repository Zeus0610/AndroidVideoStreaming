package com.zeus.videostreaming.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Preview(widthDp = 160, heightDp = 90)
@Composable
fun PlayerControls(
    isPlaying: Boolean = false,
    videoTime: Float = 0f,
    onPlayPause: () -> Unit = {},
    onTimeChange: (Float) -> Unit = {},
    playerView: @Composable () -> Unit = {}
) {
    var visibility by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = isPlaying) {
        if (isPlaying) {
            delay(3000)
            visibility = visibility.not()
        }
    }

    Box(
        modifier = Modifier.clickable { visibility = visibility.not() },
        contentAlignment = Alignment.Center
    ) {
        playerView.invoke()
        AnimatedVisibility(
            visible = visibility,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row {
                IconButton(
                    onClick = onPlayPause
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying)
                                android.R.drawable.ic_media_pause
                            else
                                android.R.drawable.ic_media_play
                        ),
                        contentDescription = "Play",
                        tint = Color.White
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = visibility,
            enter = slideInVertically {
                it / 2
            },
            exit = slideOutVertically {
                it / 2
            }
        ) {
            Row {
                IconButton(
                    onClick = onPlayPause
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying)
                                android.R.drawable.ic_media_pause
                            else
                                android.R.drawable.ic_media_play
                        ),
                        contentDescription = "play pause",
                        tint = Color.White
                    )
                }

                Slider(
                    value = videoTime,
                    onValueChange = onTimeChange
                )
            }
        }
    }
}