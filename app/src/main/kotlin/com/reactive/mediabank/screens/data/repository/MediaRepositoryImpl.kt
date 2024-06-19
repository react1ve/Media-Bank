package com.reactive.mediabank.screens.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.reactive.mediabank.screens.data.data_source.Query
import com.reactive.mediabank.screens.data.data_types.findMedia
import com.reactive.mediabank.screens.data.data_types.getAlbums
import com.reactive.mediabank.screens.data.data_types.getMedia
import com.reactive.mediabank.screens.data.data_types.getMediaByUri
import com.reactive.mediabank.screens.data.data_types.getMediaListByUris
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.repository.MediaRepository
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia.BOTH
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia.PHOTOS
import com.reactive.mediabank.screens.presentation.picker.AllowedMedia.VIDEOS
import com.reactive.mediabank.utils.Resource
import com.reactive.mediabank.utils.contentFlowObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map

class MediaRepositoryImpl(
    private val context : Context,
): MediaRepository {

    override fun getMedia() : Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            it.getMedia(mediaOrder = DEFAULT_ORDER)
        }

    override fun getMediaByType(allowedMedia : AllowedMedia) : Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = when (allowedMedia) {
                PHOTOS -> Query.PhotoQuery()
                VIDEOS -> Query.VideoQuery()
                BOTH -> Query.MediaQuery()
            }
            it.getMedia(mediaQuery = query, mediaOrder = DEFAULT_ORDER)
        }

    override fun getAlbums(mediaOrder : MediaOrder) : Flow<Resource<List<Album>>> =
        context.retrieveAlbums {
            it.getAlbums(mediaOrder = mediaOrder).toMutableList()
        }

    override suspend fun getMediaById(mediaId : Long) : Media? {
        val query = Query.MediaQuery().copy(
            bundle = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    MediaStore.MediaColumns._ID + "= ?"
                )
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(mediaId.toString())
                )
            }
        )
        return context.contentResolver.findMedia(query)
    }

    override fun getMediaByAlbumId(albumId : Long) : Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = Query.MediaQuery().copy(
                bundle = Bundle().apply {
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.BUCKET_ID + "= ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(albumId.toString())
                    )
                }
            )
            /** return@retrieveMedia */
            it.getMedia(query)
        }

    override fun getMediaByAlbumIdWithType(
        albumId : Long,
        allowedMedia : AllowedMedia,
    ) : Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = Query.MediaQuery().copy(
                bundle = Bundle().apply {
                    val mimeType = when (allowedMedia) {
                        PHOTOS -> "image%"
                        VIDEOS -> "video%"
                        BOTH -> "%/%"
                    }
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.BUCKET_ID + "= ? and " + MediaStore.MediaColumns.MIME_TYPE + " like ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(albumId.toString(), mimeType)
                    )
                }
            )
            /** return@retrieveMedia */
            it.getMedia(query)
        }

    override fun getAlbumsWithType(allowedMedia : AllowedMedia) : Flow<Resource<List<Album>>> =
        context.retrieveAlbums {
            val query = Query.AlbumQuery().copy(
                bundle = Bundle().apply {
                    val mimeType = when (allowedMedia) {
                        PHOTOS -> "image%"
                        VIDEOS -> "video%"
                        BOTH -> "%/%"
                    }
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.MIME_TYPE + " like ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(mimeType)
                    )
                }
            )
            it.getAlbums(query, mediaOrder = MediaOrder.Label(OrderType.Ascending))
        }

    override fun getMediaByUri(
        uriAsString : String,
        isSecure : Boolean,
    ) : Flow<Resource<List<Media>>> =
        context.retrieveMediaAsResource {
            val media = context.getMediaByUri(Uri.parse(uriAsString))
            /** return@retrieveMediaAsResource */
            if (media == null) {
                Resource.Error(message = "Media could not be opened")
            } else {
                val query = Query.MediaQuery().copy(
                    bundle = Bundle().apply {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_SELECTION,
                            MediaStore.MediaColumns.BUCKET_ID + "= ?"
                        )
                        putStringArray(
                            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                            arrayOf(media.albumID.toString())
                        )
                    }
                )
                Resource.Success(
                    data = if (isSecure) listOf(media) else it.getMedia(query)
                        .ifEmpty { listOf(media) })
            }
        }

    override fun getMediaListByUris(
        listOfUris : List<Uri>,
        reviewMode : Boolean,
    ) : Flow<Resource<List<Media>>> =
        context.retrieveMediaAsResource {
            var mediaList = context.getMediaListByUris(listOfUris)
            if (reviewMode) {
                val query = Query.MediaQuery().copy(
                    bundle = Bundle().apply {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_SELECTION,
                            MediaStore.MediaColumns.BUCKET_ID + "= ?"
                        )
                        putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
                        putStringArray(
                            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                            arrayOf(mediaList.first().albumID.toString())
                        )
                    }
                )
                mediaList = it.getMedia(query)
            }
            if (mediaList.isEmpty()) {
                Resource.Error(message = "Media could not be opened")
            } else {
                Resource.Success(data = mediaList)
            }
        }

    companion object {
        private val DEFAULT_ORDER = MediaOrder.Date(OrderType.Descending)
        private val URIs = arrayOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        private fun displayName(newName : String) = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
        }

        private fun relativePath(newPath : String) = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, newPath)
        }

        private fun Context.retrieveMediaAsResource(dataBody : suspend (ContentResolver) -> Resource<List<Media>>) =
            contentFlowObserver(URIs).map {
                try {
                    dataBody.invoke(contentResolver)
                } catch (e : Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()

        private fun Context.retrieveMedia(dataBody : suspend (ContentResolver) -> List<Media>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(contentResolver))
                } catch (e : Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()

        private fun Context.retrieveAlbums(dataBody : suspend (ContentResolver) -> List<Album>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(contentResolver))
                } catch (e : Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()
    }
}