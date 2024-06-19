package com.reactive.mediabank.screens.presentation.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.domain.use_case.MediaUseCases
import com.reactive.mediabank.utils.MediaState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext
    private val applicationContext : Context,
    private val mediaUseCases : MediaUseCases,
) : ViewModel() {

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState = _mediaState.asStateFlow()
    var reviewMode: Boolean = false

    var dataList: List<Uri> = emptyList()
        set(value) {
            if (value.isNotEmpty() && value != dataList) {
                getMedia(value)
            }
            field = value
        }

    var mediaId: Long = -1

    private fun getMedia(clipDataUriList: List<Uri> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clipDataUriList.isNotEmpty()) {
                mediaUseCases.getMediaListByUrisUseCase(clipDataUriList, reviewMode)
                    .flowOn(Dispatchers.IO)
                    .collectLatest { result ->
                        val data = result.data
                        if (data != null) {
                            mediaId = data.first().id
                            _mediaState.value = MediaState(media = data)
                        } else {
                            _mediaState.value = mediaFromUris()
                        }
                    }
            }
        }
    }

    private fun mediaFromUris(): MediaState {
        val list = mutableListOf<Media>()
        dataList.forEach {
            Media.createFromUri(applicationContext, it)?.let { it1 -> list.add(it1) }
        }
        return MediaState(media = list)
    }

}