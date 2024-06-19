package com.reactive.mediabank.screens.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
sealed class MediaItem : Parcelable {
    @Stable
    abstract val key: String

    @Immutable
    data class Header(
        override val key: String,
        val text: String,
        val data: List<Media>
    ) : MediaItem()

    @Immutable
    data class MediaViewItem(
        override val key: String,
        val media: Media
    ) : MediaItem()

}

val Any.isHeaderKey: Boolean
    get() = this is String && this.startsWith("header_")

val Any.isBigHeaderKey: Boolean
    get() = this is String && this.startsWith("header_big_")

val Any.isIgnoredKey: Boolean
    get() = this is String && this == "aboveGrid"