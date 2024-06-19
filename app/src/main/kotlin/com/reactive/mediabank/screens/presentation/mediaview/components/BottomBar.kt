package com.reactive.mediabank.screens.presentation.mediaview.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.util.AppBottomSheetState
import com.reactive.mediabank.screens.presentation.util.ExifMetadata
import com.reactive.mediabank.screens.presentation.util.formattedAddress
import com.reactive.mediabank.screens.presentation.util.getDate
import com.reactive.mediabank.screens.presentation.util.getLocation
import com.reactive.mediabank.screens.presentation.util.rememberAppBottomSheetState
import com.reactive.mediabank.screens.presentation.util.rememberExifInterface
import com.reactive.mediabank.screens.presentation.util.rememberExifMetadata
import com.reactive.mediabank.screens.presentation.util.rememberGeocoder
import com.reactive.mediabank.screens.presentation.util.rememberMediaInfo
import com.reactive.mediabank.screens.theme.BlackScrim
import com.reactive.mediabank.screens.theme.Shapes
import com.reactive.mediabank.utils.AlbumState
import com.reactive.mediabank.utils.Constants
import com.reactive.mediabank.utils.Constants.Animation.enterAnimation
import com.reactive.mediabank.utils.Constants.Animation.exitAnimation
import com.reactive.mediabank.utils.Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION
import com.reactive.mediabank.utils.components.DragHandle
import kotlinx.coroutines.launch

@Composable
fun BoxScope.MediaViewBottomBar(
    showDeleteButton : Boolean = true,
    bottomSheetState : AppBottomSheetState,
    albumsState : AlbumState,
    showUI : Boolean,
    paddingValues : PaddingValues,
    currentMedia : Media?,
    currentIndex : Int = 0,
    onDeleteMedia : ((Int) -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = showUI,
        enter = enterAnimation(DEFAULT_TOP_BAR_ANIMATION_DURATION),
        exit = exitAnimation(DEFAULT_TOP_BAR_ANIMATION_DURATION),
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BlackScrim)
                    )
                )
                .padding(
                    top = 24.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
        }
    }
    currentMedia?.let {
        MediaInfoBottomSheet(
            media = it,
            state = bottomSheetState,
            albumsState = albumsState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaInfoBottomSheet(
    media : Media,
    state : AppBottomSheetState,
    albumsState : AlbumState,
) {
    val scope = rememberCoroutineScope()
    val exifInterface = rememberExifInterface(media, true)
    val metadataState = rememberAppBottomSheetState()
    if (exifInterface != null) {
        val exifMetadata = rememberExifMetadata(media, exifInterface)
        val mediaInfoList = rememberMediaInfo(
            media = media,
            exifMetadata = exifMetadata,
            onLabelClick = {
                scope.launch {
                    state.hide()
                    metadataState.show()
                }
            }
        )
        if (state.isVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        state.hide()
                    }
                },
                dragHandle = { DragHandle() },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetState = state.sheetState,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                BackHandler {
                    scope.launch {
                        state.hide()
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MediaInfoDateCaptionContainer(media, exifMetadata) {
                        scope.launch {
                            state.hide()
                            metadataState.show()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (mediaInfoList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .height(32.dp),
                            text = stringResource(R.string.media_details),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        for (metadata in mediaInfoList) {
                            MediaInfoRow(
                                label = metadata.label,
                                content = metadata.content,
                                icon = metadata.icon,
                                trailingIcon = metadata.trailingIcon,
                                onClick = metadata.onClick,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
fun MediaInfoDateCaptionContainer(
    media : Media,
    exifMetadata : ExifMetadata,
    onClickEditButton : () -> Unit = {},
) {
    Column {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = Shapes.large
                )
                .padding(vertical = 16.dp)
                .padding(start = 16.dp, end = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = media.timestamp.getDate(Constants.EXIF_DATE_FORMAT),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                val defaultDesc = stringResource(R.string.image_add_description)
                val imageDesc = remember(exifMetadata) {
                    val lensDesc = exifMetadata.lensDescription
                    val imageCapt = exifMetadata.imageDescription
                    return@remember if (lensDesc != null && !imageCapt.isNullOrBlank() && imageCapt != lensDesc) {
                        "$lensDesc\n$imageCapt"
                    } else lensDesc ?: (imageCapt ?: defaultDesc)
                }
                SelectionContainer {
                    Text(
                        text = imageDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!media.readUriOnly) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = onClickEditButton
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(id = R.string.edit_cd),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (media.isRaw) {
                MediaInfoChip(
                    text = media.fileExtension.toUpperCase(Locale.current),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (exifMetadata.formattedCords != null) {
                val geocoder = rememberGeocoder()
                val clipboardManager : ClipboardManager = LocalClipboardManager.current
                var locationName by remember { mutableStateOf(exifMetadata.formattedCords!!) }
                LaunchedEffect(geocoder) {
                    geocoder?.getLocation(
                        exifMetadata.gpsLatLong!![0],
                        exifMetadata.gpsLatLong[1]
                    ) { address ->
                        address?.let {
                            val addressName = it.formattedAddress
                            if (addressName.isNotEmpty()) {
                                locationName = addressName
                            }
                        }
                    }
                }
                MediaInfoChip(
                    text = stringResource(R.string.location_chip, locationName),
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(exifMetadata.formattedCords!!))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaInfoChip(
    text : String,
    contentColor : Color = MaterialTheme.colorScheme.onSecondaryContainer,
    containerColor : Color = MaterialTheme.colorScheme.secondaryContainer,
    outlineInLightTheme : Boolean = true,
    onClick : () -> Unit = {},
    onLongClick : () -> Unit = {},
) {
    Text(
        modifier = Modifier
            .background(
                color = containerColor,
                shape = Shapes.extraLarge
            )
            .then(
                if (!isSystemInDarkTheme() && outlineInLightTheme) Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = Shapes.extraLarge
                ) else Modifier
            )
            .clip(Shapes.extraLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = contentColor
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarColumn(
    currentMedia : Media?,
    imageVector : ImageVector,
    title : String,
    followTheme : Boolean = false,
    onItemLongClick : ((Media) -> Unit)? = null,
    onItemClick : (Media) -> Unit,
) {
    val tintColor = if (followTheme) MaterialTheme.colorScheme.onSurface else Color.White
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(
                minWidth = 90.dp,
                minHeight = 80.dp
            )
            .combinedClickable(
                onLongClick = {
                    currentMedia?.let {
                        onItemLongClick?.invoke(it)
                    }
                },
                onClick = {
                    currentMedia?.let {
                        onItemClick.invoke(it)
                    }
                }
            )
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = imageVector,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = title,
            modifier = Modifier
                .height(32.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title,
            modifier = Modifier,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = tintColor,
            textAlign = TextAlign.Center
        )
    }
}