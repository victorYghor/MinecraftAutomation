package net.kdt.pojavlaunch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pixelmon.Loading

class LauncherActivityViewModel: ViewModel() {
    private var _currentLoadingState = MutableLiveData<Loading>()

    fun setLoadingState(loading: Loading) {
        when(loading) {
            Loading.MOVING_FILES -> {

            }
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> TODO()
            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> TODO()
            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> TODO()
            Loading.SHOW_PLAY_BUTTON -> TODO()
            Loading.DOWNLOAD_TEXTURE -> TODO()
        }

    }

}