package com.reactive.mediabank.screens.presentation.mediaview.components.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Scale
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.utils.coil.coil3
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableImage
import me.saket.telephoto.zoomable.ZoomableImageSource
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.engawapg.lib.zoomable.rememberZoomState

@Composable
fun ZoomablePagerImage(
    modifier: Modifier = Modifier,
    media: Media,
    uiEnabled: Boolean,
    maxScale: Float = 10f,
    onItemClick: () -> Unit
) {
    val zoomState = rememberZoomState(
        maxScale = maxScale,
    )
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(media.uri)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .placeholderMemoryCacheKey(media.toString())
            .scale(Scale.FILL)
            .build(),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
        onSuccess = {
            zoomState.setContentSize(it.painter.intrinsicSize)
        }
    )
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(
            maxZoomFactor = maxScale
        )
    )
    val state = rememberZoomableImageState(
        zoomableState = zoomableState
    )

    Box(modifier = Modifier.fillMaxSize()) {

        ZoomableImage(
            modifier = modifier.fillMaxSize(),
            onClick = { onItemClick() },
            state = state,
            image = ZoomableImageSource.coil3(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(media.uri)
                    .scale(Scale.FILL)
                    .placeholderMemoryCacheKey(media.toString())
                    .build()
            ),
            contentScale = ContentScale.Fit,
            contentDescription = media.label
        )
    }


}
