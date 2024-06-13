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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import pixelmon.Loading
import pixelmon.MinecraftAssets
import pixelmon.SupportFile
import pixelmon.Tools.Timberly
import pixelmon.Tools.checkFileIntegrity
import pixelmon.Tools.deleteDirecoty
import pixelmon.Tools.md5
import pixelmon.download.Downloader
import pixelmon.mods.PixelmonVersion
import timber.log.Timber
import java.io.File

class LauncherViewModel(
    private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Is not possible to modify inside the class because here I don't have a observe for call the
     * function setLoadingState
     */
    val loadingState = MutableLiveData<Loading>()
    val bottomButtonsVisible = MutableLiveData<Boolean>()
    val callPixelmonLoading = MutableLiveData(false)
    val selectedPixelmonVersion = MutableLiveData(PixelmonVersion.OneDotTwelve)
    /**
     * this will be trigger when the app complete download the version 1.16 and then this will
     * possible the user choose the version of forge that want to play
     * this value need to be change insider the Discpatchers.Main
     */
    val downloadOneDotSixteen = MutableLiveData(LauncherPreferences.DOWNLOAD_ONE_DOT_SIXTEEN)
    val downloadModOneDotSixteen = MutableLiveData(LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_SIXTEEN)
    val mDownloader by lazy {
        MutableLiveData(Downloader(context, this))
    }
    private val loadingStateObserver = Observer<Loading> { newLoading ->
        this.setLoadingState(newLoading)
    }
    /**
     * Use this for showing if one dot sixteen already was downloaded, don't use the shared preferences
     * this will update de shared preferences when the value changed
     */
    private val downloadOneDotSixteenObserver = Observer<Boolean> { downloaded ->
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_one_dot_sixteen", downloaded)
            .commit()
        LauncherPreferences.loadPreferences(context)
    }
    private val downloadModOneDotSixteenObserver = Observer<Boolean> { downloaded ->
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_sixteen", downloaded).commit()
        LauncherPreferences.loadPreferences(context)
    }

    init {
        loadingState.observeForever(loadingStateObserver)
        downloadOneDotSixteen.observeForever(downloadOneDotSixteenObserver)
        downloadModOneDotSixteen.observeForever(downloadModOneDotSixteenObserver)
    }

    fun setLoadingState(loading: Loading) {
        when (loading) {
            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> {
                Timber.tag(Timberly.downloadProblem).d("start the download of mods 1.12")
                ProgressLayout.setProgress(
                    ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                    0,
                    Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE.messageLoading
                )
                val downloadScope = CoroutineScope(Dispatchers.IO)
                downloadScope.launch {
                    // espera ate que o download dos mods seja completo
                    // para que a próxima parte do código começe a rodar
                    mDownloader.value?.downloadModsOneDotTwelve()?.join()
                    LauncherPreferences.DEFAULT_PREF.edit()
                        .putBoolean("download_mod_one_dot_twelve", true).commit()
                    Timber.tag("downloadProblem").d("finish the download of mods 1.12")
                    withContext(Dispatchers.Main) {
                        loadingState.value = Loading.DOWNLOAD_TEXTURE
                    }
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
            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    mDownloader.value?.downloadModsOneDotSixteen()?.join()
                    withContext(Dispatchers.Main) {
                        downloadModOneDotSixteen.value = true
                        loadingState.value = Loading.SHOW_PLAY_BUTTON
                    }
                }
                return
            }
            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        mDownloader.value?.downloadOneDotSixteen()?.await()
                        // tudo isso precisa ser feito quando o download for concluido
                        // aqui é necessário verificar integridade do download
                        // se estiver tudo bem com o download ele deve continuar se não um pop up deve aparecer para avisar isso ao usuário
                        val librariesZipFile = File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")
                        // pegando a referencia da bliblioteca no formato json.
                        val libraryFile = Tools.GLOBAL_GSON.fromJson(
                            read(context.assets.open("support-files.json")),
                            SupportFile::class.java
                        )
                        val integrity = checkFileIntegrity(context, librariesZipFile.md5(), libraryFile.md5)
                        Timber.d("integrity of the file is " + integrity)
                        // se o arquivo não tiver intregidade eu preciso avisar para o usuário que houve um problema
                        // com um arquivo deletar ele e perdir para ele fazer o download novamente
                        deleteDirecoty(File(context.getExternalFilesDir(null), ".minecraft/libraries"))
                        mDownloader.value?.unpackLibraries(librariesZipFile)
                        withContext(Dispatchers.Main) {
                            downloadOneDotSixteen.value = true
                            loadingState.value = Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN
                        }
                    }
                return
            }
            Loading.SHOW_PLAY_BUTTON -> {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
                return
            }
            Loading.DOWNLOAD_TEXTURE -> {
                CoroutineScope(Dispatchers.Default).launch {
                    mDownloader.value?.downloadTexture()?.await()
                    LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_texture", true)
                        .commit()
                }
                return
            }
        }

    }

    fun changeProfile(ctx: Context, pixelmonVersion: PixelmonVersion) {
        Timber.w("select the correct option")
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, pixelmonVersion.key)
            .commit()
        LauncherPreferences.loadPreferences(ctx)
        val profile = LauncherPreferences.DEFAULT_PREF.getString(
            LauncherPreferences.PREF_KEY_CURRENT_PROFILE,
            ""
        )
        Timber.w("The current profile is " + profile)
        LauncherProfiles.load()
    }
    fun renameModsFiles(ctx: Context, modVersion: PixelmonVersion) {

    }

     fun setupPixelmonLoading() {
        if (callPixelmonLoading.value == false) {
            val getOneDotTwelve = LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_TWELVE
            Timber.d("the value of getOneDotTwelve is " + getOneDotTwelve)
            if (getOneDotTwelve) {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
            } else {
                this.loadingState.value = Loading.MOVING_FILES
            }
            callPixelmonLoading.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadingState.removeObserver(loadingStateObserver)
    }
    companion object {
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
}
