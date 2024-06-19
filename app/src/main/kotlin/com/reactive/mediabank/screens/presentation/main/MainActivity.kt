package com.reactive.mediabank.screens.presentation.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.reactive.mediabank.screens.presentation.mediaview.MediaViewScreen
import com.reactive.mediabank.screens.presentation.util.newImageLoader
import com.reactive.mediabank.screens.presentation.util.toggleOrientation
import com.reactive.mediabank.screens.theme.MediabankTheme
import com.reactive.mediabank.utils.AlbumState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val action = intent.action.toString()
        val isSecure = action.lowercase().contains("secure")
        val clipData = intent.clipData
        val uriList = mutableSetOf<Uri>()
        intent.data?.let(uriList::add)
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                uriList.add(clipData.getItemAt(i).uri)
            }
        }
        setShowWhenLocked(isSecure)
        setContent {
            setSingletonImageLoaderFactory(::newImageLoader)
            MediabankTheme(darkTheme = true) {
                Scaffold { paddingValues ->
                    val viewModel = hiltViewModel<MainViewModel>().apply {
                        reviewMode = action.lowercase().contains("review")
                        dataList = uriList.toList()
                    }

                    MediaViewScreen(
                        navigateUp = { finish() },
                        toggleRotate = ::toggleOrientation,
                        paddingValues = paddingValues,
                        isStandalone = true,
                        mediaId = viewModel.mediaId,
                        mediaState = viewModel.mediaState,
                        albumsState = MutableStateFlow(AlbumState()),
                    )
                }
                BackHandler {
                    finish()
                }
            }
        }
    }
}