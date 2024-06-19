package com.reactive.mediabank.screens.domain.use_case

import android.content.Context
import com.reactive.mediabank.screens.domain.repository.MediaRepository

data class MediaUseCases(
    private val context: Context,
    private val repository: MediaRepository
) {
    val getAlbumsUseCase = GetAlbumsUseCase(repository)
    val getAlbumsWithTypeUseCase = GetAlbumsWithTypeUseCase(repository)
    val getMediaUseCase = GetMediaUseCase(repository)
    val getMediaByAlbumUseCase = GetMediaByAlbumUseCase(repository)
    val getMediaByAlbumWithTypeUseCase = GetMediaByAlbumWithTypeUseCase(repository)
    val getMediaByTypeUseCase = GetMediaByTypeUseCase(repository)
    val getMediaListByUrisUseCase = GetMediaListByUrisUseCase(repository)
}