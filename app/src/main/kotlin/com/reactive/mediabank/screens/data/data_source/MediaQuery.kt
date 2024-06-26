package com.reactive.mediabank.screens.data.data_source

import android.content.ContentResolver
import android.os.Bundle
import android.provider.MediaStore

sealed class Query(
    var projection: Array<String>,
    var bundle: Bundle? = null
) {
    class MediaQuery : Query(
        projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.MIME_TYPE,
        ),
    )

    class PhotoQuery: Query(
        projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.MIME_TYPE,
        ),
        bundle = defaultBundle.apply {
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                MediaStore.MediaColumns.MIME_TYPE + " like ?"
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf("image%")
            )
        }
    )

    class VideoQuery: Query(
        projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.ORIENTATION,
        ),
        bundle = defaultBundle.apply {
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                MediaStore.MediaColumns.MIME_TYPE + " LIKE ?"
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf("video%")
            )
        }
    )

    class AlbumQuery : Query(
        projection = arrayOf(
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATE_TAKEN
        )
    )

    fun copy(
        projection: Array<String> = this.projection,
        bundle: Bundle? = this.bundle,
    ): Query {
        this.projection = projection
        this.bundle = bundle
        return this
    }

    companion object {
        val defaultBundle = Bundle().apply {
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.MediaColumns.DATE_MODIFIED)
            )
            putInt(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
        }
    }

}