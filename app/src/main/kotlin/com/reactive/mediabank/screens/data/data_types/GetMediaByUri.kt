package com.reactive.mediabank.screens.data.data_types

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.reactive.mediabank.screens.data.data_source.Query.MediaQuery
import com.reactive.mediabank.screens.domain.model.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.getMediaByUri(uri: Uri): Media? {
    return withContext(Dispatchers.IO) {
        var media: Media? = null
        val mediaQuery = MediaQuery().copy(
            bundle = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    MediaStore.MediaColumns.DATA + "=?"
                )
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(uri.toString())
                )
            }
        )
        with(contentResolver.query(mediaQuery)) {
            moveToFirst()
            while (!isAfterLast) {
                try {
                    media = getMediaFromCursor()
                    break
                } catch (e: Exception) {
                    close()
                    e.printStackTrace()
                }
            }
            moveToNext()
            close()
        }
        if (media == null) {
            media = Media.createFromUri(this@getMediaByUri, uri)
        }

        return@withContext media
    }
}

suspend fun Context.getMediaListByUris(list: List<Uri>): List<Media> {
    return withContext(Dispatchers.IO) {
        val mediaList = ArrayList<Media>()
        val mediaQuery = MediaQuery().copy(
            bundle = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    MediaStore.MediaColumns._ID + "=?"
                )
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    list.map { it.toString().substringAfterLast("/") }.toTypedArray()
                )
            }
        )
        mediaList.addAll(contentResolver.getMedia(mediaQuery))
        if (mediaList.isEmpty()) {
            for (uri in list) {
                Media.createFromUri(this@getMediaListByUris, uri)?.let { mediaList.add(it) }
            }
        }
        mediaList
    }
}