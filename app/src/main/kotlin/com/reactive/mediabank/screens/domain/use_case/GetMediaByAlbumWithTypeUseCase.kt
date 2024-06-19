package com.reactive.mediabank.screens.domain.use_case

import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMediaByAlbumWithTypeUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        albumId: Long,
        type: AllowedMedia,
        mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaByAlbumIdWithType(albumId, type).map {
            it.apply {
                data = data?.let { it1 -> mediaOrder.sortMedia(it1) }
            }
        }
    }

}

