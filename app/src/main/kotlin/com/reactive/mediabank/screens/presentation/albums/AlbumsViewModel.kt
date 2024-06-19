package com.reactive.mediabank.screens.presentation.albums

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reactive.mediabank.screens.domain.model.Album
import com.reactive.mediabank.screens.domain.use_case.MediaUseCases
import com.reactive.mediabank.screens.domain.util.MediaOrder
import com.reactive.mediabank.screens.domain.util.OrderType
import com.reactive.mediabank.screens.presentation.util.RepeatOnResume
import com.reactive.mediabank.screens.presentation.util.Screen
import com.reactive.mediabank.utils.AlbumState
import com.reactive.mediabank.utils.Resource
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

    private fun getAlbums(mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaUseCases.getAlbumsUseCase(mediaOrder).collectLatest { result ->
                // Result data list
                val data = result.data ?: emptyList()
                val error =
                    if (result is Resource.Error) result.message ?: "An error occurred" else ""
                val newAlbumState = AlbumState(error = error, albums = data)
                if (data == albumsState.value.albums) return@collectLatest

                if (albumsState.value != newAlbumState) {
                    _albumsState.emit(newAlbumState)
                }
            }
        }
    }

}