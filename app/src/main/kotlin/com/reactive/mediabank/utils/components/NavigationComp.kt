package com.reactive.mediabank.utils.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.presentation.albums.AlbumsScreen
import com.reactive.mediabank.screens.presentation.albums.AlbumsViewModel
import com.reactive.mediabank.screens.presentation.common.ChanneledViewModel
import com.reactive.mediabank.screens.presentation.common.MediaViewModel
import com.reactive.mediabank.screens.presentation.mediaview.MediaViewScreen
import com.reactive.mediabank.screens.presentation.setup.SetupScreen
import com.reactive.mediabank.screens.presentation.timeline.TimelineScreen
import com.reactive.mediabank.screens.presentation.util.Screen
import com.reactive.mediabank.screens.presentation.util.newImageLoader
import com.reactive.mediabank.utils.Constants
import com.reactive.mediabank.utils.Constants.Animation.navigateInAnimation
import com.reactive.mediabank.utils.Constants.Animation.navigateUpAnimation
import com.reactive.mediabank.utils.Constants.Target.TARGET_FAVORITES
import com.reactive.mediabank.utils.ObserveCustomMediaState
import com.reactive.mediabank.utils.OnLifecycleEvent
import com.reactive.mediabank.utils.Settings.Album.rememberHideTimelineOnAlbum
import com.reactive.mediabank.utils.Settings.Misc.rememberLastScreen
import com.reactive.mediabank.utils.Settings.Misc.rememberTimelineGroupByMonth
import com.reactive.mediabank.utils.permissionGranted

@OptIn(ExperimentalCoilApi::class)
@Composable
fun NavigationComp(
    navController : NavHostController,
    paddingValues : PaddingValues,
    bottomBarState : MutableState<Boolean>,
    systemBarFollowThemeState : MutableState<Boolean>,
    toggleRotate : () -> Unit,
    isScrolling : MutableState<Boolean>,
) {
    val searchBarActive = rememberSaveable {
        mutableStateOf(false)
    }
    val bottomNavEntries = rememberNavigationItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val navPipe = hiltViewModel<ChanneledViewModel>()
    navPipe
        .initWithNav(navController, bottomBarState)
        .collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val groupTimelineByMonth by rememberTimelineGroupByMonth()

    val context = LocalContext.current
    var permissionState by remember { mutableStateOf(context.permissionGranted(Constants.PERMISSIONS)) }
    var lastStartScreen by rememberLastScreen()
    val startDest = remember(permissionState, lastStartScreen) {
        if (permissionState) {
            lastStartScreen
        } else Screen.SetupScreen()
    }
    val currentDest = remember(navController.currentDestination) {
        navController.currentDestination?.route ?: lastStartScreen
    }
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            if (currentDest == Screen.TimelineScreen() || currentDest == Screen.AlbumsScreen()) {
                lastStartScreen = currentDest
            }
        }
    }

    var lastShouldDisplay by rememberSaveable {
        mutableStateOf(bottomNavEntries.find { item -> item.route == currentDest } != null)
    }
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let {
            val shouldDisplayBottomBar =
                bottomNavEntries.find { item -> item.route == it } != null
            if (lastShouldDisplay != shouldDisplayBottomBar) {
                bottomBarState.value = shouldDisplayBottomBar
                lastShouldDisplay = shouldDisplayBottomBar
            }
            systemBarFollowThemeState.value = !it.contains(Screen.MediaViewScreen.route)
        }
    }

    // Preloaded viewModels
    val albumsViewModel = hiltViewModel<AlbumsViewModel>().apply {
        attachToLifecycle()
    }

    val timelineViewModel = hiltViewModel<MediaViewModel>().apply {
        attachToLifecycle()
    }
    LaunchedEffect(groupTimelineByMonth) {
        timelineViewModel.groupByMonth = groupTimelineByMonth
    }

    setSingletonImageLoaderFactory(::newImageLoader)

    NavHost(
        navController = navController,
        startDestination = startDest,
        enterTransition = { navigateInAnimation },
        exitTransition = { navigateUpAnimation },
        popEnterTransition = { navigateInAnimation },
        popExitTransition = { navigateUpAnimation }
    ) {
        composable(
            route = Screen.SetupScreen(),
        ) {
            navPipe.toggleNavbar(false)
            SetupScreen {
                permissionState = true
                navPipe.navigate(Screen.TimelineScreen())
            }
        }
        composable(
            route = Screen.TimelineScreen()
        ) {
            TimelineScreen(
                vm = timelineViewModel,
                paddingValues = paddingValues,
                mediaState = timelineViewModel.mediaState,
                albumState = albumsViewModel.albumsState,
                selectionState = timelineViewModel.multiSelectState,
                selectedMedia = timelineViewModel.selectedPhotoState,
                toggleSelection = timelineViewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                isScrolling = isScrolling,
                searchBarActive = searchBarActive,
            )
        }
        composable(
            route = Screen.AlbumsScreen()
        ) {
            AlbumsScreen(
                navigate = navPipe::navigate,
                paddingValues = paddingValues,
                viewModel = albumsViewModel,
                isScrolling = isScrolling
            )
        }
        composable(
            route = Screen.AlbumViewScreen.albumAndName(),
            arguments = listOf(
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val appName = stringResource(id = R.string.app_name)
            val argumentAlbumName = remember(backStackEntry) {
                backStackEntry.arguments?.getString("albumName") ?: appName
            }
            val argumentAlbumId = remember(backStackEntry) {
                backStackEntry.arguments?.getLong("albumId") ?: -1
            }
            timelineViewModel.ObserveCustomMediaState {
                getMediaFromAlbum(argumentAlbumId)
            }
            val hideTimeline by rememberHideTimelineOnAlbum()
            TimelineScreen(
                vm = timelineViewModel,
                paddingValues = paddingValues,
                albumId = argumentAlbumId,
                albumName = argumentAlbumName,
                mediaState = timelineViewModel.customMediaState,
                albumState = albumsViewModel.albumsState,
                selectionState = timelineViewModel.multiSelectState,
                selectedMedia = timelineViewModel.selectedPhotoState,
                allowNavBar = false,
                allowHeaders = !hideTimeline,
                enableStickyHeaders = !hideTimeline,
                toggleSelection = timelineViewModel::toggleCustomSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                isScrolling = isScrolling
            )
        }
        composable(
            route = Screen.MediaViewScreen.idAndAlbum(),
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val mediaId : Long = remember(backStackEntry) {
                backStackEntry.arguments?.getLong("mediaId") ?: -1L
            }
            val albumId : Long = remember(backStackEntry) {
                backStackEntry.arguments?.getLong("albumId") ?: -1L
            }
            if (albumId != -1L) {
                timelineViewModel.ObserveCustomMediaState {
                    getMediaFromAlbum(albumId)
                }
            }
            MediaViewScreen(
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate,
                paddingValues = paddingValues,
                mediaId = mediaId,
                mediaState = if (albumId != -1L) timelineViewModel.customMediaState else timelineViewModel.mediaState,
                albumsState = albumsViewModel.albumsState,
            )
        }
        composable(
            route = Screen.MediaViewScreen.idAndTarget(),
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "target") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mediaId : Long = remember(backStackEntry) {
                backStackEntry.arguments?.getLong("mediaId") ?: -1
            }
            val target : String? = remember(backStackEntry) {
                backStackEntry.arguments?.getString("target")
            }
            val entryName = remember(target) {
                Screen.TimelineScreen.route
            }
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(entryName)
            }
            val viewModel = if (target == TARGET_FAVORITES) {
                timelineViewModel.also {
                    timelineViewModel.ObserveCustomMediaState(MediaViewModel::getFavoriteMedia)
                }
            } else {
                hiltViewModel<MediaViewModel>(parentEntry).also {
                    it.attachToLifecycle()
                }
            }
            MediaViewScreen(
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate,
                paddingValues = paddingValues,
                mediaId = mediaId,
                target = target,
                mediaState = if (target == TARGET_FAVORITES) viewModel.customMediaState else viewModel.mediaState,
                albumsState = albumsViewModel.albumsState,
            )
        }
    }
}