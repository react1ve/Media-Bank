package com.reactive.mediabank.utils

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.model.MediaItem
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class MediaState(
    val media: List<Media> = emptyList(),
    val mappedMedia: List<MediaItem> = emptyList(),
    val mappedMediaWithMonthly: List<MediaItem> = emptyList(),
    val dateHeader: String = "",
    val error: String = "",
    val isLoading: Boolean = true
) : Parcelable


@Immutable
@Parcelize
data class AlbumState(
    val albums: List<Album> = emptyList(),
    val error: String = ""
) : Parcelable