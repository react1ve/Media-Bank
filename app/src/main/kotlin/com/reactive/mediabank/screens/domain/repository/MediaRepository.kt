package com.reactive.mediabank.screens.domain.repository

import android.net.Uri
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia
import com.reactive.mediabank.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MediaRepository {

    fun getMedia() : Flow<Resource<List<Media>>>

    fun getMediaByType(allowedMedia : AllowedMedia) : Flow<Resource<List<Media>>>

    fun getAlbums(mediaOrder : MediaOrder) : Flow<Resource<List<Album>>>

    suspend fun getMediaById(mediaId : Long) : Media?

    fun getMediaByAlbumId(albumId : Long) : Flow<Resource<List<Media>>>

    fun getMediaByAlbumIdWithType(
        albumId : Long,
        allowedMedia : AllowedMedia,
    ) : Flow<Resource<List<Media>>>

    fun getAlbumsWithType(allowedMedia : AllowedMedia) : Flow<Resource<List<Album>>>

    fun getMediaByUri(uriAsString : String, isSecure : Boolean) : Flow<Resource<List<Media>>>

    fun getMediaListByUris(
        listOfUris : List<Uri>,
        reviewMode : Boolean,
    ) : Flow<Resource<List<Media>>>

}