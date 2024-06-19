package com.reactive.mediabank.screens.presentation.albums

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.use_case.MediaUseCases
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import com.reactive.mediabank.screens.presentation.util.RepeatOnResume
import com.reactive.mediabank.screens.presentation.util.Screen
import com.reactive.mediabank.utils.AlbumState
import com.reactive.mediabank.utils.Resource
import com.reactive.mediabank.utils.Settings
import com.reactive.mediabank.utils.components.FilterOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    private val _albumsState = MutableStateFlow(AlbumState())
    val albumsState = _albumsState.asStateFlow()
    private val _unPinnedAlbumsState = MutableStateFlow(AlbumState())
    val unPinnedAlbumsState = _unPinnedAlbumsState.asStateFlow()
    private val _pinnedAlbumState = MutableStateFlow(AlbumState())
    val pinnedAlbumState = _pinnedAlbumState.asStateFlow()

    fun onAlbumClick(navigate: (String) -> Unit): (Album) -> Unit = { album ->
        navigate(Screen.AlbumViewScreen.route + "?albumId=${album.id}&albumName=${album.label}")
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun attachToLifecycle() {
        RepeatOnResume {
            getAlbums()
        }
    }

    @Composable
    fun rememberFilters(): SnapshotStateList<FilterOption> {
        val lastValue by Settings.Album.rememberLastSort()
        return remember(lastValue) {
            mutableStateListOf(
                FilterOption(
                    titleRes = R.string.filter_recent,
                    mediaOrder = MediaOrder.Date(OrderType.Descending),
                    onClick = { updateOrder(it) },
                    selected = lastValue == 0
                ),
                FilterOption(
                    titleRes = R.string.filter_old,
                    mediaOrder = MediaOrder.Date(OrderType.Ascending),
                    onClick = { updateOrder(it) },
                    selected = lastValue == 1
                ),

                FilterOption(
                    titleRes = R.string.filter_nameAZ,
                    mediaOrder = MediaOrder.Label(OrderType.Ascending),
                    onClick = { updateOrder(it) },
                    selected = lastValue == 2
                ),
                FilterOption(
                    titleRes = R.string.filter_nameZA,
                    mediaOrder = MediaOrder.Label(OrderType.Descending),
                    onClick = { updateOrder(it) },
                    selected = lastValue == 3
                )
            )
        }
    }

    private fun updateOrder(mediaOrder: MediaOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            val newState = unPinnedAlbumsState.value.copy(
                albums = mediaOrder.sortAlbums(unPinnedAlbumsState.value.albums)
            )
            if (unPinnedAlbumsState.value != newState) {
                _unPinnedAlbumsState.emit(newState)
            }
        }
    }

    private fun toggleAlbumPin(album: Album, isPinned: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAlbum = album.copy(isPinned = isPinned)
            if (isPinned) {
                // Remove original Album from unpinned List
                _unPinnedAlbumsState.emit(
                    unPinnedAlbumsState.value.copy(
                        albums = unPinnedAlbumsState.value.albums.minus(album)
                    )
                )
                // Add 'pinned' version of the album object to the pinned List
                _pinnedAlbumState.emit(pinnedAlbumState.value.copy(
                    albums = pinnedAlbumState.value.albums.toMutableList().apply { add(newAlbum) }
                ))
            } else {
                // Add 'un-pinned' version of the album object to the pinned List
                _unPinnedAlbumsState.emit(unPinnedAlbumsState.value.copy(
                    albums = unPinnedAlbumsState.value.albums.toMutableList().apply { add(newAlbum) }
                ))
                // Remove original Album from pinned List
                _pinnedAlbumState.emit(
                    pinnedAlbumState.value.copy(
                        albums = pinnedAlbumState.value.albums.minus(album)
                    )
                )
            }
        }
    }

    private fun getAlbums(mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaUseCases.getAlbumsUseCase(mediaOrder).collectLatest { result ->
                // Result data list
                val data = result.data ?: emptyList()
                val error =
                    if (result is Resource.Error) result.message ?: "An error occurred" else ""
                val newAlbumState = AlbumState(error = error, albums = data)
                if (data == albumsState.value.albums) return@collectLatest
                val newUnPinnedAlbumState = AlbumState(error = error, albums = data.filter { !it.isPinned })
                val newPinnedState = AlbumState(
                    error = error,
                    albums = data.filter { it.isPinned }.sortedBy { it.label })
                if (unPinnedAlbumsState.value != newUnPinnedAlbumState) {
                    _unPinnedAlbumsState.emit(newUnPinnedAlbumState)
                }
                if (pinnedAlbumState.value != newPinnedState) {
                    _pinnedAlbumState.emit(newPinnedState)
                }
                if (albumsState.value != newAlbumState) {
                    _albumsState.emit(newAlbumState)
                }
            }
        }
    }

}