package com.reactive.mediabank.screens.domain.use_case

import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetAlbumsUseCase(
    private val repository : MediaRepository,
) {

    operator fun invoke(
        mediaOrder : MediaOrder = MediaOrder.Date(OrderType.Descending),
    ) : Flow<Resource<List<Album>>> = repository.getAlbums(mediaOrder)
}