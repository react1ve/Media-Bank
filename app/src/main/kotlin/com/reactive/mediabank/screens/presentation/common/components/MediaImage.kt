package com.reactive.mediabank.screens.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Scale
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.model.MediaEqualityDelegate
import com.reactive.mediabank.screens.presentation.mediaview.components.video.VideoDurationHeader
import com.reactive.mediabank.utils.Constants.Animation

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaImage(
    modifier : Modifier = Modifier,
    media : Media,
    selectionState : MutableState<Boolean>,
    canClick : Boolean,
    onItemClick : (Media) -> Unit,
    onItemLongClick : (Media) -> Unit,
) {

    val selectedSize by animateDpAsState(
        0.dp, label = "selectedSize"
    )
    val scale by animateFloatAsState(
        1f, label = "scale"
    )
    val selectedShapeSize by animateDpAsState(
        0.dp, label = "selectedShapeSize"
    )
    val strokeSize by animateDpAsState(
        targetValue = 0.dp, label = "strokeSize"
    )
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val strokeColor by animateColorAsState(
        targetValue = Color.Transparent,
        label = "strokeColor"
    )
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(media.uri)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .placeholderMemoryCacheKey(media.toString())
            .scale(Scale.FIT)
            .build(),
        modelEqualityDelegate = MediaEqualityDelegate(),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None
    )
    Box(
        modifier = modifier
            .combinedClickable(
                enabled = canClick,
                onClick = {
                    onItemClick(media)
                },
                onLongClick = {
                    onItemLongClick(media)
                },
            )
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(1f)
                .padding(selectedSize)
                .clip(RoundedCornerShape(selectedShapeSize))
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(selectedShapeSize)
                )
                .border(
                    width = strokeSize,
                    shape = RoundedCornerShape(selectedShapeSize),
                    color = strokeColor
                )
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = painter,
                contentDescription = media.label,
                contentScale = ContentScale.Crop,
            )
        }

        AnimatedVisibility(
            visible = remember(media) {
                media.duration != null
            },
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            VideoDurationHeader(
                modifier = Modifier
                    .padding(selectedSize / 2)
                    .scale(scale),
                media = media
            )
        }
    }
}
