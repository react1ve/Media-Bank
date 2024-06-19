package com.reactive.mediabank.screens.presentation.mediaview.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.util.ExifMetadata
import com.reactive.mediabank.screens.presentation.util.formatMinSec
import com.reactive.mediabank.screens.presentation.util.formattedFileSize
import com.reactive.mediabank.screens.theme.Shapes
import com.reactive.mediabank.utils.Constants.TAG
import java.io.File
import java.io.IOException

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaInfoRow(
    modifier: Modifier = Modifier,
    label: String,
    content: String,
    icon: ImageVector,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.medium)
            .combinedClickable(
                onClick = { onClick?.let { it() } },
                onLongClick = {
                    if (onLongClick != null) onLongClick()
                    else {
                        clipboardManager.setText(AnnotatedString(content))
                    }
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(text = content)
        },
        trailingContent = if (trailingIcon != null) {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = contentDescription
                )
            }
        } else null,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    )
}

data class InfoRow(
    val label: String,
    val content: String,
    val icon: ImageVector,
    val trailingIcon: ImageVector? = null,
    val contentDescription: String? = null,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
)

fun Media.retrieveMetadata(context: Context, exifMetadata: ExifMetadata, onLabelClick: () -> Unit): List<InfoRow> {
    val infoList = ArrayList<InfoRow>()

    try {
        infoList.apply {
            if (!exifMetadata.modelName.isNullOrEmpty()) {
                val aperture = exifMetadata.apertureValue
                val focalLength = exifMetadata.focalLength
                val isoValue = exifMetadata.isoValue
                val stringBuilder = StringBuilder()
                if (aperture != 0.0)
                    stringBuilder.append("f/$aperture")
                if (focalLength != 0.0)
                    stringBuilder.append(" • ${focalLength}mm")
                if (isoValue != 0)
                    stringBuilder.append(context.getString(R.string.iso) + isoValue)
                add(
                    InfoRow(
                        icon = Icons.Outlined.Camera,
                        label = "${exifMetadata.manufacturerName} ${exifMetadata.modelName}",
                        content = stringBuilder.toString()
                    )
                )
            }
            add(
                InfoRow(
                    icon = Icons.Outlined.Photo,
                    trailingIcon = Icons.Outlined.Edit,
                    label = context.getString(R.string.label),
                    onClick = onLabelClick,
                    content = label
                )
            )
            val formattedFileSize = File(path).formattedFileSize(context)
            val contentString = StringBuilder()
            contentString.append(formattedFileSize)
            if (mimeType.contains("video")) {
                contentString.append(" • ${duration.formatMinSec()}")
            } else if (exifMetadata.imageWidth != 0 && exifMetadata.imageHeight != 0) {
                val width = exifMetadata.imageWidth
                val height = exifMetadata.imageHeight
                val imageMp = exifMetadata.imageMp
                if (imageMp > "0") contentString.append(" • $imageMp MP")
                if (width > 0 && height > 0) contentString.append(" • $width x $height")
            }
            val icon = if (mimeType.contains("video")) Icons.Outlined.VideoFile else Icons.Outlined.ImageSearch
            add(
                InfoRow(
                    icon = icon,
                    label = context.getString(R.string.metadata),
                    content = contentString.toString()
                )
            )


            add(
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = context.getString(R.string.path),
                    content = path.substringBeforeLast("/")
                )
            )
        }
    } catch (e: IOException) {
        Log.e(TAG, "ExifInterface ERROR\n" + e.printStackTrace())
    }

    return infoList
}