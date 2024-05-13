package com.zeus.videostreaming.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.media.MediaRouter
import com.zeus.videostreaming.R
import com.zeus.videostreaming.utils.findActivity
import com.zeus.videostreaming.viewmodel.CastViewModel

@Composable
fun CastButton(
    modifier: Modifier,
) {
    val context = LocalContext.current.apply {
        setTheme(R.style.Theme_mMediaRouter)
    }
    val viewModel = viewModel<CastViewModel> {
        CastViewModel(context)
    }

    AndroidView(
        modifier = modifier,
        factory = {
            //Se crea el boton de cast
            val mediaRouteActionProvider = MediaRouteActionProvider(context)
            mediaRouteActionProvider.routeSelector = viewModel.mediarouterSelector
            mediaRouteActionProvider.onCreateActionView()
        }
    )
    
    DisposableEffect(key1 = LocalContext.current.findActivity()) {
        viewModel.mediarouterSelector.also {
            viewModel.mediaRouter.addCallback(it, viewModel.mediaRouteCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY
            )
        }
        onDispose {
            viewModel.mediaRouter.removeCallback(viewModel.mediaRouteCallback)
        }
    }
}
