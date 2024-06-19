package com.reactive.mediabank.screens.domain.use_case

import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetAlbumsWithTypeUseCase(
    private val repository : MediaRepository,
) {

    operator fun invoke(
        allowedMedia : AllowedMedia,
    ) : Flow<Resource<List<Album>>> = repository.getAlbumsWithType(allowedMedia)
}