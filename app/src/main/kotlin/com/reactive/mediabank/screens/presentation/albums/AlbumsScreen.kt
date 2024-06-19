package com.reactive.mediabank.screens.presentation.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.pinchzoomgrid.PinchZoomGridLayout
import com.dokar.pinchzoomgrid.rememberPinchZoomGridState
import com.reactive.mediabank.screens.presentation.albums.components.AlbumComponent
import com.reactive.mediabank.utils.Constants.albumCellsList
import com.reactive.mediabank.utils.Settings.Album.rememberAlbumGridSize
import com.reactive.mediabank.utils.components.EmptyMedia
import com.reactive.mediabank.utils.components.Error

@Composable
fun AlbumsScreen(
    navigate : (route : String) -> Unit,
    paddingValues : PaddingValues,
    viewModel : AlbumsViewModel,
    isScrolling : MutableState<Boolean>,
) {
    val state by viewModel.unPinnedAlbumsState.collectAsStateWithLifecycle()
    var lastCellIndex by rememberAlbumGridSize()

    val pinchState = rememberPinchZoomGridState(
        cellsList = albumCellsList,
        initialCellsIndex = lastCellIndex
    )

    LaunchedEffect(pinchState.isZooming) {
        lastCellIndex = albumCellsList.indexOf(pinchState.currentCells)
    }

    Scaffold {
        PinchZoomGridLayout(state = pinchState) {
            LaunchedEffect(gridState.isScrollInProgress) {
                isScrolling.value = gridState.isScrollInProgress
            }
            LazyVerticalGrid(
                state = gridState,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
                columns = gridCells,
                contentPadding = PaddingValues(
                    top = it.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.albums,
                    key = { item -> item.toString() }
                ) { item ->
                    AlbumComponent(
                        modifier = Modifier.pinchItem(key = item.toString()),
                        album = item,
                        onItemClick = viewModel.onAlbumClick(navigate),
                    )
                }
            }
        }
        /** Error State Handling Block **/
        if (state.error.isNotEmpty()) {
            Error(errorMessage = state.error)
        } else if (state.albums.isEmpty()) {
            EmptyMedia(modifier = Modifier.fillMaxSize())
        }
        /** ************ **/
    }
}