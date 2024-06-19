package com.reactive.mediabank.screens.presentation.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.use_case.MediaUseCases
import com.reactive.mediabank.screens.presentation.util.RepeatOnResume
import com.reactive.mediabank.screens.presentation.util.collectMedia
import com.reactive.mediabank.screens.presentation.util.mediaFlow
import com.reactive.mediabank.screens.presentation.util.update
import com.reactive.mediabank.utils.MediaState
import com.reactive.mediabank.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import javax.inject.Inject

@HiltViewModel
open class MediaViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    var lastQuery = mutableStateOf("")
        private set
    val multiSelectState = mutableStateOf(false)
    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState = _mediaState.asStateFlow()
    private val _customMediaState = MutableStateFlow(MediaState())
    val customMediaState = _customMediaState.asStateFlow()
    private val _searchMediaState = MutableStateFlow(MediaState())
    val searchMediaState = _searchMediaState.asStateFlow()
    val selectedPhotoState = mutableStateListOf<Media>()

    var albumId: Long = -1L
    var target: String? = null

    var groupByMonth: Boolean = false
        set(value) {
            field = value
            if (field != value) {
                getMedia(albumId, target)
            }
        }

    @SuppressLint("ComposableNaming")
    @Composable
    fun attachToLifecycle() {
        RepeatOnResume {
            getMedia(albumId, target)
        }
    }

    private suspend fun List<Media>.parseQuery(query: String): List<Media> {
        return withContext(Dispatchers.IO) {
            if (query.isEmpty())
                return@withContext emptyList()
            val matches = FuzzySearch.extractSorted(query, this@parseQuery, { it.toString() }, 60)
            return@withContext matches.map { it.referent }.ifEmpty { emptyList() }
        }
    }

    fun toggleCustomSelection(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = customMediaState.value.media[index]
            val selectedPhoto = selectedPhotoState.find { it.id == item.id }
            if (selectedPhoto != null) {
                selectedPhotoState.remove(selectedPhoto)
            } else {
                selectedPhotoState.add(item)
            }
            multiSelectState.update(selectedPhotoState.isNotEmpty())
        }
    }

    fun toggleSelection(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = mediaState.value.media[index]
            val selectedPhoto = selectedPhotoState.find { it.id == item.id }
            if (selectedPhoto != null) {
                selectedPhotoState.remove(selectedPhoto)
            } else {
                selectedPhotoState.add(item)
            }
            multiSelectState.update(selectedPhotoState.isNotEmpty())
        }
    }

    fun getMediaFromAlbum(albumId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = mediaState.value.media.filter { it.albumID == albumId }
            val error = mediaState.value.error
            if (error.isNotEmpty()) {
                return@launch _customMediaState.emit(MediaState(isLoading = false, error = error))
            }
            if (data.isEmpty()) {
                return@launch _customMediaState.emit(MediaState(isLoading = false))
            }
            _customMediaState.collectMedia(
                data = data,
                error = mediaState.value.error,
                albumId = albumId,
                groupByMonth = groupByMonth
            )
        }
    }

    fun getFavoriteMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = mediaState.value.media
            val error = mediaState.value.error
            if (error.isNotEmpty()) {
                return@launch _customMediaState.emit(MediaState(isLoading = false, error = error))
            }
            if (data.isEmpty()) {
                return@launch _customMediaState.emit(MediaState(isLoading = false))
            }
            _customMediaState.collectMedia(
                data = data,
                error = mediaState.value.error,
                albumId = -1L,
                groupByMonth = groupByMonth
            )
        }
    }

    fun queryMedia(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                lastQuery.value = query
            }
            if (query.isEmpty()) {
                _searchMediaState.tryEmit(MediaState(isLoading = false))
                return@launch
            } else {
                _searchMediaState.tryEmit(MediaState(isLoading = true))
                return@launch _searchMediaState.collectMedia(
                    data = mediaState.value.media.parseQuery(query),
                    error = mediaState.value.error,
                    albumId = albumId,
                    groupByMonth = groupByMonth
                )
            }
        }
    }

    private fun getMedia(albumId: Long = -1L, target: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaUseCases.mediaFlow(albumId).collectLatest { result ->
                val data = result.data ?: emptyList()
                val error = if (result is Resource.Error) result.message
                    ?: "An error occurred" else ""
                if (error.isNotEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState(isLoading = false, error = error))
                }
                if (data.isEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState(isLoading = false))
                }
                if (data == _mediaState.value.media) return@collectLatest
                return@collectLatest _mediaState.collectMedia(
                    data = data,
                    error = error,
                    albumId = albumId,
                    groupByMonth = groupByMonth,
                )
            }
        }
    }

}