package com.reactive.mediabank.screens.presentation.timeline.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reactive.mediabank.R
import com.reactive.mediabank.screens.domain.model.Media
import com.reactive.mediabank.screens.presentation.common.components.OptionItem
import com.reactive.mediabank.screens.presentation.common.components.OptionSheet
import com.reactive.mediabank.screens.presentation.util.rememberAppBottomSheetState
import com.reactive.mediabank.utils.MediaState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun TimelineNavActions(
    albumId: Long,
    expandedDropDown: MutableState<Boolean>,
    mediaState: StateFlow<MediaState>,
    selectedMedia: SnapshotStateList<Media>,
    selectionState: MutableState<Boolean>,
    navigate: (route: String) -> Unit,
    navigateUp: () -> Unit
) {
    val state by mediaState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val result = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                selectedMedia.clear()
                selectionState.value = false
            }
        }
    )
    val context = LocalContext.current
    val appBottomSheetState = rememberAppBottomSheetState()
    LaunchedEffect(appBottomSheetState.isVisible, expandedDropDown.value) {
        scope.launch {
            if (expandedDropDown.value) appBottomSheetState.show()
            else appBottomSheetState.hide()
        }
    }
    val optionList = remember(selectionState.value) {
        mutableListOf(
            OptionItem(
                text = if (selectionState.value)
                    context.getString(R.string.unselect_all)
                else
                    context.getString(R.string.select_all),
                onClick = {
                    selectionState.value = !selectionState.value
                    if (selectionState.value)
                        selectedMedia.addAll(state.media)
                    else
                        selectedMedia.clear()
                    expandedDropDown.value = false
                }
            )
        )
    }
    IconButton(onClick = { expandedDropDown.value = !expandedDropDown.value }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.drop_down_cd)
        )
    }

    OptionSheet(
        state = appBottomSheetState,
        onDismiss = {
            expandedDropDown.value = false
        },
        optionList = arrayOf(optionList)
    )
}

