package com.reactive.mediabank.screens.domain.use_case

import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetMediaUseCase(
    private val repository : MediaRepository,
) {

    operator fun invoke() : Flow<Resource<List<Media>>> = repository.getMedia()

}