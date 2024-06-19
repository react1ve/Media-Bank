package com.reactive.mediabank.screens.presentation.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.exifinterface.media.ExifInterface
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.mediaview.components.InfoRow
import com.reactive.mediabank.screens.presentation.mediaview.components.retrieveMetadata
import java.io.IOException

@Composable
fun rememberMediaInfo(media: Media, exifMetadata: ExifMetadata, onLabelClick: () -> Unit): List<InfoRow> {
    val context = LocalContext.current
    return remember(media) {
        media.retrieveMetadata(context, exifMetadata, onLabelClick)
    }
}

@Composable
fun rememberExifMetadata(media: Media, exifInterface: ExifInterface): ExifMetadata {
    return remember(media) {
        ExifMetadata(exifInterface)
    }
}

@Composable
fun rememberExifInterface(media: Media, useDirectPath: Boolean = false): ExifInterface? {
    val context = LocalContext.current
    return remember(media) {
        if (useDirectPath) try { ExifInterface(media.path) } catch (_: IOException) { null }
        else getExifInterface(context, media.uri)
    }
}

@Throws(IOException::class)
fun getExifInterface(context: Context, uri: Uri): ExifInterface? {
    if (uri.isFromApps()) return null
    return try {
        ExifInterface(context.uriToPath(uri).toString())
    } catch (_: IOException) {
        null
    }
}

fun Context.uriToPath(uri: Uri?): String? {
    if (uri == null) return null
    val proj = arrayOf(MediaStore.MediaColumns.DATA)
    var path: String? = null
    val cursor: Cursor? = contentResolver.query(uri, proj, null, null, null)
    if (cursor != null && cursor.count != 0) {
        cursor.moveToFirst()
        path = try {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            cursor.getString(columnIndex)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
    cursor?.close()
    return path ?: FileUtils(this).getPath(uri)
}
