package com.reactive.mediabank.screens.domain.model

data class PlaybackSpeed(
    val speed: Float,
    val label: String,
    val isAuto: Boolean = false
)
