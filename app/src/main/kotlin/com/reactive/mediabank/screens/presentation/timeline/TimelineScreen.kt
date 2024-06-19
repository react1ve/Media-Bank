package com.reactive.mediabank.screens.presentation.timeline

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.common.MediaScreen
import com.reactive.mediabank.screens.presentation.common.MediaViewModel
import com.reactive.mediabank.screens.presentation.timeline.components.TimelineNavActions
import com.reactive.mediabank.utils.AlbumState
import com.reactive.mediabank.utils.MediaState
import com.reactive.mediabank.utils.components.EmptyMedia
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TimelineScreen(
    paddingValues: PaddingValues,
    albumId: Long = -1L,
    albumName: String = stringResource(R.string.app_name),
    vm: MediaViewModel,
    mediaState: StateFlow<MediaState>,
    albumState: StateFlow<AlbumState>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    allowNavBar: Boolean = true,
    allowHeaders: Boolean = true,
    enableStickyHeaders: Boolean = true,
    toggleSelection: (Int) -> Unit,
    navigate: (route: String) -> Unit,
    navigateUp: () -> Unit,
    toggleNavbar: (Boolean) -> Unit,
    isScrolling: MutableState<Boolean>,
    searchBarActive: MutableState<Boolean> = mutableStateOf(false)
) {
    MediaScreen(
        paddingValues = paddingValues,
        albumId = albumId,
        target = null,
        albumName = albumName,
        vm = vm,
        albumState = albumState,
        mediaState = mediaState,
        selectionState = selectionState,
        selectedMedia = selectedMedia,
        toggleSelection = toggleSelection,
        allowHeaders = allowHeaders,
        showMonthlyHeader = true,
        enableStickyHeaders = enableStickyHeaders,
        allowNavBar = allowNavBar,
        navActionsContent = { expandedDropDown: MutableState<Boolean>, _ ->
            TimelineNavActions(
                albumId = albumId,
                expandedDropDown = expandedDropDown,
                mediaState = mediaState,
                selectedMedia = selectedMedia,
                selectionState = selectionState,
                navigate = navigate,
                navigateUp = navigateUp
            )
        },
        emptyContent = { EmptyMedia(Modifier.fillMaxSize()) },
        navigate = navigate,
        navigateUp = navigateUp,
        toggleNavbar = toggleNavbar,
        isScrolling = isScrolling
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedMedia.clear()
            selectionState.value = false
        }
    }
}