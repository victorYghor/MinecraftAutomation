package net.kdt.pojavlaunch

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.kdt.mcgui.ProgressLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Loading
import pixelmon.MinecraftAssets
import pixelmon.download.Downloader

class LauncherViewModel(
    private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    /**
     * Is not possible to modify inside the class because here I don't have a observe for call the
     * function setLoadingState
     */
    val loadingState = MutableLiveData<Loading>()
    private val loadingStateObserver = Observer<Loading> { newLoading ->
        this.setLoadingState(newLoading)
    }
    val mDownloader by lazy {
        MutableLiveData(Downloader(context, this))
    }

    companion object {
        private const val TAG = "LauncherActivityViewModel"
        fun provideFactory(
            context: Context,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return LauncherViewModel(context, handle) as T
                }
            }
    }

    fun setLoadingState(loading: Loading) {
        when (loading) {
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> {
                Log.d(TAG, "start the download of mods 1.12")
                ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE, 0, Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE.messageLoading)
                val downloadScope = CoroutineScope(Dispatchers.Default)
                downloadScope.launch {
                    mDownloader.value?.downloadModsOneDotTwelve()
                    LauncherPreferences.DEFAULT_PREF.edit()
                        .putBoolean("download_mod_one_dot_twelve", true).commit()
                }
                return
            }

            Loading.MOVING_FILES -> {
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("get_one_dot_twelve", true)
                    .commit()

                ProgressLayout.setProgress(
                    ProgressLayout.MOVING_FILES,
                    0,
                    Loading.MOVING_FILES.messageLoading
                )
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        MinecraftAssets(context, this@LauncherViewModel).moveImportantAssets()
                    }
                }
                return
            }

            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> TODO()
            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> TODO()
            Loading.SHOW_PLAY_BUTTON -> {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
            }

            Loading.DOWNLOAD_TEXTURE -> TODO()
        }

    }

    init {
        loadingState.observeForever(loadingStateObserver)
    }

    override fun onCleared() {
        super.onCleared()
        loadingState.removeObserver(loadingStateObserver)
    }

}