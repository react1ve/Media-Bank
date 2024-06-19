package com.reactive.mediabank.screens.domain.use_case

import android.net.Uri
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetMediaListByUrisUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        listOfUris: List<Uri>,
        reviewMode: Boolean
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaListByUris(listOfUris, reviewMode)
    }

}

