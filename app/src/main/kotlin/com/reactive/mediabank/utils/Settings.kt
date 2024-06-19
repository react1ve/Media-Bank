package com.reactive.mediabank.utils

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reactive.mediabank.screens.presentation.util.Screen
import com.reactive.mediabank.utils.Settings.PREFERENCE_NAME
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

object Settings {

    const val PREFERENCE_NAME = "settings"

    object Album {
        private val LAST_SORT = intPreferencesKey("album_last_sort")

        @Composable
        fun rememberLastSort() =
            rememberPreference(key = LAST_SORT, defaultValue = 0)

        @Composable
        fun rememberAlbumGridSize(): MutableState<Int> {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val prefs = remember(context) {
                context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            }
            var storedSize = remember(prefs) {
                prefs.getInt("album_grid_size", 3)
            }

            return remember(storedSize) {
                object : MutableState<Int> {
                    override var value: Int
                        get() = storedSize
                        set(value) {
                            scope.launch {
                                prefs.edit {
                                    putInt("album_grid_size", value)
                                    storedSize = value
                                }
                            }
                        }

                    override fun component1() = value
                    override fun component2(): (Int) -> Unit = { value = it }
                }
            }
        }

        private val HIDE_TIMELINE_ON_ALBUM = booleanPreferencesKey("hide_timeline_on_album")

        @Composable
        fun rememberHideTimelineOnAlbum() =
            rememberPreference(key = HIDE_TIMELINE_ON_ALBUM, defaultValue = false)
    }

    object Misc {
        private val USER_CHOICE_MEDIA_MANAGER = booleanPreferencesKey("use_media_manager")

        @RequiresApi(Build.VERSION_CODES.S)
        @Composable
        fun rememberIsMediaManager() =
            rememberPreference(
                key = USER_CHOICE_MEDIA_MANAGER, defaultValue = MediaStore.canManageMedia(
                    LocalContext.current
                )
            )

        private val LAST_SCREEN = stringPreferencesKey("last_screen")

        @Composable
        fun rememberLastScreen() =
            rememberPreference(key = LAST_SCREEN, defaultValue = Screen.TimelineScreen())

        private val FORCED_LAST_SCREEN = booleanPreferencesKey("forced_last_screen")

        @Composable
        fun rememberForcedLastScreen() =
            rememberPreference(key = FORCED_LAST_SCREEN, defaultValue = false)

        @Composable
        fun rememberGridSize(): MutableState<Int> {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val prefs = remember(context) {
                context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            }
            var storedSize = remember(prefs) {
                prefs.getInt("media_grid_size", 3)
            }

            return remember(storedSize) {
                object : MutableState<Int> {
                    override var value: Int
                        get() = storedSize
                        set(value) {
                            scope.launch {
                                prefs.edit {
                                    putInt("media_grid_size", value)
                                    storedSize = value
                                }
                            }
                        }

                    override fun component1() = value
                    override fun component2(): (Int) -> Unit = { value = it }
                }
            }
        }

        private val FORCE_THEME = booleanPreferencesKey("force_theme")

        @Composable
        fun rememberForceTheme() =
            rememberPreference(key = FORCE_THEME, defaultValue = false)

        private val DARK_MODE = booleanPreferencesKey("dark_mode")

        @Composable
        fun rememberIsDarkMode() =
            rememberPreference(key = DARK_MODE, defaultValue = false)

        private val AMOLED_MODE = booleanPreferencesKey("amoled_mode")

        @Composable
        fun rememberIsAmoledMode() =
            rememberPreference(key = AMOLED_MODE, defaultValue = false)

        private val SECURE_MODE = booleanPreferencesKey("secure_mode")

        @Composable
        fun rememberSecureMode() =
            rememberPreference(key = SECURE_MODE, defaultValue = false)

        fun getSecureMode(context: Context) =
            context.dataStore.data.map { it[SECURE_MODE] ?: false }

        private val TIMELINE_GROUP_BY_MONTH = booleanPreferencesKey("timeline_group_by_month")

        @Composable
        fun rememberTimelineGroupByMonth() =
            rememberPreference(key = TIMELINE_GROUP_BY_MONTH, defaultValue = false)

        private val ALLOW_BLUR = booleanPreferencesKey("allow_blur")

        @Composable
        fun rememberAllowBlur() = rememberPreference(key = ALLOW_BLUR, defaultValue = true)

        private val OLD_NAVBAR = booleanPreferencesKey("old_navbar")

        @Composable
        fun rememberOldNavbar() = rememberPreference(key = OLD_NAVBAR, defaultValue = false)

        private val ALLOW_VIBRATIONS = booleanPreferencesKey("allow_vibrations")

        fun allowVibrations(context: Context) =
            context.dataStore.data.map { it[ALLOW_VIBRATIONS] ?: true }
    }
}
