package com.reactive.mediabank.screens.presentation.util

sealed class Screen(val route: String) {
    data object TimelineScreen : Screen("timeline_screen")
    data object AlbumsScreen : Screen("albums_screen")

    data object AlbumViewScreen : Screen("album_view_screen") {

        fun albumAndName() = "$route?albumId={albumId}&albumName={albumName}"

    }
    data object MediaViewScreen : Screen("media_screen") {

        fun idAndTarget() = "$route?mediaId={mediaId}&target={target}"

        fun idAndAlbum() = "$route?mediaId={mediaId}&albumId={albumId}"
    }

    data object SetupScreen: Screen("setup_screen")

    operator fun invoke() = route
}
