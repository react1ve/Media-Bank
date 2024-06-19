package com.reactive.mediabank.screens.presentation.albums.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Scale
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.model.MediaEqualityDelegate
import com.reactive.mediabank.utils.AutoResizeText
import com.reactive.mediabank.utils.FontSizeRange

@Composable
fun AlbumComponent(
    modifier : Modifier = Modifier,
    album : Album,
    isEnabled : Boolean = true,
    onItemClick : (Album) -> Unit,
) {
    Column(
        modifier = modifier
            .alpha(if (isEnabled) 1f else 0.4f)
            .padding(horizontal = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
        ) {
            AlbumImage(
                album = album,
                isEnabled = isEnabled,
                onItemClick = onItemClick,
            )
            if (album.isOnSdcard) {
                Icon(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                        .align(Alignment.BottomEnd),
                    imageVector = Icons.Outlined.SdCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        AutoResizeText(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 16.dp),
            text = album.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontSizeRange = FontSizeRange(
                min = 10.sp,
                max = 16.sp
            )
        )
        if (album.count > 0) {
            AutoResizeText(
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                text = pluralStringResource(
                    id = R.plurals.item_count,
                    count = album.count.toInt(),
                    album.count
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                fontSizeRange = FontSizeRange(
                    min = 6.sp,
                    max = 12.sp
                )
            )
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumImage(
    album : Album,
    isEnabled : Boolean,
    onItemClick : (Album) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val radius = 16.dp
    val cornerRadius by animateDpAsState(targetValue = radius, label = "cornerRadius")
    if (album.id == -200L && album.count == 0L) {
        Icon(
            imageVector = Icons.Outlined.AddCircleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .alpha(0.8f)
                .clip(RoundedCornerShape(cornerRadius))
                .combinedClickable(
                    enabled = isEnabled,
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = { onItemClick(album) },
                )
                .padding(48.dp)
        )
    } else {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(album.uri)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .placeholderMemoryCacheKey(album.toString())
                .scale(Scale.FIT)
                .build(),
            modelEqualityDelegate = MediaEqualityDelegate(),
            contentScale = ContentScale.FillBounds
        )
        Image(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .combinedClickable(
                    enabled = isEnabled,
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = { onItemClick(album) },
                ),
            painter = painter,
            contentDescription = album.label,
            contentScale = ContentScale.Crop,
        )
    }
}