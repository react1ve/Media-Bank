package com.reactive.mediabank.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reactive.mediabank.screens.presentation.common.MediaViewModel

@Composable
fun MediaViewModel.ObserveCustomMediaState(onChange: MediaViewModel.() -> Unit) {
    val state by mediaState.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        onChange()
    }
}