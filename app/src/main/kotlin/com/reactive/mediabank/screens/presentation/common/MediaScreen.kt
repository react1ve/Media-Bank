package com.reactive.mediabank.screens.presentation.common

import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.pinchzoomgrid.PinchZoomGridLayout
import com.dokar.pinchzoomgrid.rememberPinchZoomGridState
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.common.components.MediaGridView
import com.reactive.mediabank.screens.presentation.common.components.TwoLinedDateToolbarTitle
import com.reactive.mediabank.screens.presentation.util.Screen
import com.reactive.mediabank.utils.AlbumState
import com.reactive.mediabank.utils.Constants.Animation.enterAnimation
import com.reactive.mediabank.utils.Constants.Animation.exitAnimation
import com.reactive.mediabank.utils.Constants.cellsList
import com.reactive.mediabank.utils.MediaState
import com.reactive.mediabank.utils.Settings.Misc.rememberGridSize
import com.reactive.mediabank.utils.components.Error
import com.reactive.mediabank.utils.components.LoadingMedia
import com.reactive.mediabank.utils.components.NavigationButton
import kotlinx.coroutines.flow.StateFlow

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MediaScreen(
    paddingValues : PaddingValues,
    albumId : Long = -1L,
    target : String? = null,
    albumName : String,
    vm : MediaViewModel,
    albumState : StateFlow<AlbumState>,
    mediaState : StateFlow<MediaState>,
    selectionState : MutableState<Boolean>,
    selectedMedia : SnapshotStateList<Media>,
    toggleSelection : (Int) -> Unit,
    allowHeaders : Boolean = true,
    showMonthlyHeader : Boolean = false,
    enableStickyHeaders : Boolean = true,
    allowNavBar : Boolean = false,
    navActionsContent : @Composable() (RowScope.(expandedDropDown : MutableState<Boolean>, result : ActivityResultLauncher<IntentSenderRequest>) -> Unit),
    emptyContent : @Composable () -> Unit,
    aboveGridContent : @Composable() (() -> Unit)? = null,
    navigate : (route : String) -> Unit,
    navigateUp : () -> Unit,
    toggleNavbar : (Boolean) -> Unit,
    isScrolling : MutableState<Boolean> = remember { mutableStateOf(false) },
    onActivityResult : (result : ActivityResult) -> Unit,
) {
    var canScroll by rememberSaveable { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        canScroll = { canScroll },
        flingAnimationSpec = null
    )
    var lastCellIndex by rememberGridSize()

    val pinchState = rememberPinchZoomGridState(
        cellsList = cellsList,
        initialCellsIndex = lastCellIndex
    )

    LaunchedEffect(pinchState.isZooming) {
        canScroll = !pinchState.isZooming
        lastCellIndex = cellsList.indexOf(pinchState.currentCells)
    }

    /** STATES BLOCK **/
    val state by mediaState.collectAsStateWithLifecycle()
    val albumsState by albumState.collectAsStateWithLifecycle()
    /** ************ **/

    /** Selection state handling **/
    LaunchedEffect(LocalConfiguration.current, selectionState.value) {
        if (allowNavBar) {
            toggleNavbar(!selectionState.value)
        }
    }
    /** ************  **/
    Box {
        Scaffold(
            modifier = Modifier,
            topBar = {
                LargeTopAppBar(
                    title = {
                        TwoLinedDateToolbarTitle(
                            albumName = albumName,
                            dateHeader = state.dateHeader
                        )
                    },
                    navigationIcon = {
                        NavigationButton(
                            albumId = albumId,
                            target = target,
                            navigateUp = navigateUp,
                            clearSelection = {
                                selectionState.value = false
                                selectedMedia.clear()
                            },
                            selectionState = selectionState,
                            alwaysGoBack = true,
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { it ->
            PinchZoomGridLayout(state = pinchState) {
                AnimatedVisibility(
                    visible = state.isLoading,
                    enter = enterAnimation,
                    exit = exitAnimation
                ) {
                    LoadingMedia(
                        paddingValues = PaddingValues(
                            top = it.calculateTopPadding(),
                            bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
                        )
                    )
                }
                MediaGridView(
                    mediaState = state,
                    allowSelection = true,
                    searchBarPaddingTop = paddingValues.calculateTopPadding(),
                    enableStickyHeaders = enableStickyHeaders,
                    paddingValues = PaddingValues(
                        top = it.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
                    ),
                    canScroll = canScroll,
                    selectionState = selectionState,
                    selectedMedia = selectedMedia,
                    allowHeaders = allowHeaders,
                    showMonthlyHeader = showMonthlyHeader,
                    toggleSelection = toggleSelection,
                    aboveGridContent = aboveGridContent,
                    isScrolling = isScrolling
                ) {
                    val albumRoute = "albumId=$albumId"
                    val targetRoute = "target=$target"
                    val param =
                        if (target != null) targetRoute else albumRoute
                    navigate(Screen.MediaViewScreen.route + "?mediaId=${it.id}&$param")
                }
                /** Error State Handling Block **/
                val showError = remember(state) { state.error.isNotEmpty() }
                AnimatedVisibility(visible = showError) {
                    Error(errorMessage = state.error)
                }
                val showEmpty =
                    remember(state) { state.media.isEmpty() && !state.isLoading && !showError }
                AnimatedVisibility(visible = showEmpty) {
                    emptyContent.invoke()
                }
                /** ************ **/
            }
        }
    }
}