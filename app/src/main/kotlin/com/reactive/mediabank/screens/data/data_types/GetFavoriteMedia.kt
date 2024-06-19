package com.reactive.mediabank.screens.data.data_types

import android.content.ContentResolver
import android.provider.MediaStore
import com.reactive.mediabank.screens.data.data_source.Query.Companion.defaultBundle
import com.reactive.mediabank.screens.data.data_source.Query.MediaQuery
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ContentResolver.getMediaFavorite(
    mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
): List<Media> {
    return withContext(Dispatchers.IO) {
        val mediaQuery = MediaQuery().copy(
            bundle = defaultBundle.apply {
                putInt(MediaStore.QUERY_ARG_MATCH_FAVORITE, MediaStore.MATCH_ONLY)
            }
        )
        return@withContext mediaOrder.sortMedia(getMedia(mediaQuery))
    }
}
