package com.reactive.mediabank.screens.domain.use_case

import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetMediaByUriUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        uriAsString: String,
        isSecure: Boolean = false
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaByUri(uriAsString, isSecure)
    }

}

