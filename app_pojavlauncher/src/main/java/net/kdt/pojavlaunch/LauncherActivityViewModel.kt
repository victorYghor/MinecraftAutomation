package net.kdt.pojavlaunch

import android.annotation.SuppressLint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kdt.mcgui.ProgressLayout
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Loading
import pixelmon.MinecraftAssets

class LauncherActivityViewModel(application: PojavApplication):  AndroidViewModel(application){
    private var _currentLoadingState = MutableLiveData<Loading>()

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<PojavApplication>().applicationContext

    fun setLoadingState(loading: Loading) {
        when(loading) {
            Loading.MOVING_FILES -> {
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("get_one_dot_twelve", true).commit()
                Thread {
                    ProgressLayout.setProgress(ProgressLayout.MOVING_FILES, 0, Loading.MOVING_FILES.messageLoading);
                    MinecraftAssets(context).run()
                }.start()
                ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE)
            }
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> TODO()
            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> TODO()
            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> TODO()
            Loading.SHOW_PLAY_BUTTON -> TODO()
            Loading.DOWNLOAD_TEXTURE -> TODO()
        }

    }

}