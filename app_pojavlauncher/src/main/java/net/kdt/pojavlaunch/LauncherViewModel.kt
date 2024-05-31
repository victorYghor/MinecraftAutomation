package net.kdt.pojavlaunch

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kdt.mcgui.ProgressLayout
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Loading
import pixelmon.MinecraftAssets
import pixelmon.download.Downloader

class LauncherViewModel():  ViewModel(){
    /**
     * Is not possible to modify inside the class because here I don't have a observe for call the
     * function setLoadingState
     */
    val loadingState = MutableLiveData<Loading>()
    companion object {
        private const val TAG = "LauncherActivityViewModel"
    }
    fun setLoadingState(loading: Loading, context: Context) {
        val mDownloader by lazy {
            MutableLiveData(Downloader(context))
        }
        when(loading) {
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE ->  {
                Log.d(TAG, "start the download of mods 1.12")
                mDownloader.value?.downloadModsOneDotTwelve()
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_twelve", true).commit()
            }
            Loading.MOVING_FILES -> {
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("get_one_dot_twelve", true).commit()
                Thread {
                    ProgressLayout.setProgress(ProgressLayout.MOVING_FILES, 0, Loading.MOVING_FILES.messageLoading);
                    MinecraftAssets(context).run()
                }.start()
                loadingState.value = Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE
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

    }
}