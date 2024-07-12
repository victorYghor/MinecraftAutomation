package net.kdt.pojavlaunch

import android.content.Context
import android.os.Bundle
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
    val selectedPixelmonVersion = MutableLiveData<PixelmonVersion>()

    /**
     * this will be trigger when the app complete download the version 1.16 and then this will
     * possible the user choose the version of forge that want to play
     * this value need to be change insider the Discpatchers.Main
     */
    val downloadOneDotSixteen = MutableLiveData(LauncherPreferences.DOWNLOAD_ONE_DOT_SIXTEEN)
    val downloadModOneDotSixteen = MutableLiveData(LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_SIXTEEN)
    val downloadModOneDotTwelve = MutableLiveData(LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_TWELVE)
    val getOneDotTwelve = MutableLiveData(LauncherPreferences.GET_ONE_DOT_TWELVE)
    val downloadTexture = MutableLiveData(LauncherPreferences.DOWNLOAD_TEXTURE)

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
    private val downloadOneDotSixteenObserver = Observer<Boolean> {
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean("download_one_dot_sixteen", it).commit()
        LauncherPreferences.loadPreferences(context)
    }
    private val downloadModOneDotSixteenObserver = Observer<Boolean> {
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean("download_mod_one_dot_sixteen", it).commit()
        LauncherPreferences.loadPreferences(context)
    }
    private val getOneDotTwelveObserver = Observer<Boolean> { it ->
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean("get_one_dot_twelve", it).commit()
        LauncherPreferences.loadPreferences(context)
    }
    private val downloadModOneDotTwelveObserver = Observer<Boolean> {
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean("download_mod_one_dot_twelve", it).commit()
        LauncherPreferences.loadPreferences(context)
    }
    private val downloadTextureObserver = Observer<Boolean> {
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean("download_texture", it).commit()
        LauncherPreferences.loadPreferences(context)
    }

    /**
     * This observer need change of profile and change the dir name and update the preferences
     */
    val selectVersionObserver = Observer<PixelmonVersion> {
        updatePreferencesPixelmonVersion(it)
        changeProfile(context, it)
        // todo se ele não conseguir terminar de fazer o que precisa isso daqui não vai ser chamado na troca de versão
        if (downloadModOneDotSixteen.value == true) {
            renameModsFiles(context, it)
            overrideConfigurationsFiles(context, it)
            overrideTexture(context, it)
        }
    }

    init {
        loadingState.observeForever(loadingStateObserver)

        getOneDotTwelve.observeForever(getOneDotTwelveObserver)
        downloadOneDotSixteen.observeForever(downloadOneDotSixteenObserver)
        downloadModOneDotSixteen.observeForever(downloadModOneDotSixteenObserver)
        downloadModOneDotTwelve.observeForever(downloadModOneDotTwelveObserver)
        downloadTexture.observeForever(downloadTextureObserver)
        selectedPixelmonVersion.observeForever(selectVersionObserver)

        if (downloadModOneDotSixteen.value == false) {
            selectedPixelmonVersion.value = PixelmonVersion.OneDotTwelve
        } else {
            // serach in the preferences what is the current version
            selectedPixelmonVersion.value =
                if (LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE) {
                    PixelmonVersion.OneDotTwelve
                } else {
                    PixelmonVersion.OneDotSixteen
                }
        }
    }

    fun setLoadingState(loading: Loading) {
        when (loading) {
            Loading.MOVING_FILES -> {
                if (getOneDotTwelve.value == false) {
                    ProgressLayout.setProgress(
                        ProgressLayout.MOVING_FILES,
                        0,
                        Loading.MOVING_FILES.messageLoading
                    )
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            MinecraftAssets(context, this@LauncherViewModel).moveImportantAssets()
                                .join()
                            withContext(Dispatchers.Main) {
                                getOneDotTwelve.value = true
                                loadingState.value = Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE
                            }
                        }
                    }
                }
                return
            }

            Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE -> {
                if (downloadModOneDotTwelve.value == false) {
                    Timber.tag(Timberly.downloadProblem).d("start the download of mods 1.12")
                    ProgressLayout.setProgress(
                        ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                        0,
                        Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE.messageLoading
                    )
                    viewModelScope.launch {
                        // espera ate que o download dos mods seja completo
                        // para que a próxima parte do código começe a rodar
                        mDownloader.value?.downloadModsOneDotTwelve()?.join()
                        Timber.tag("downloadProblem").d("finish the download of mods 1.12")
                        withContext(Dispatchers.Main) {
                            downloadModOneDotTwelve.value = true
                            loadingState.value = Loading.DOWNLOAD_TEXTURE
                        }
                    }
                }
                return
            }

            Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN -> {
                if (downloadOneDotSixteen.value == false) {
                    viewModelScope.launch {
                        mDownloader.value?.downloadModsOneDotSixteen()?.join()
//                    mDownloader.value?.putTextureInOneDotSixteen()?.join()
                        withContext(Dispatchers.Main) {
                            downloadModOneDotSixteen.value = true
                            loadingState.value = Loading.DOWNLOAD_TEXTURE
                        }
                    }
                }
                return
            }

            Loading.DOWNLOAD_ONE_DOT_SIXTEEN -> {
                if (downloadOneDotSixteen.value == false) {
                    CoroutineScope(Dispatchers.IO).launch {
                        mDownloader.value?.downloadOneDotSixteen()?.await()
                        val librariesZipFile =
                            File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")
                        // pegando a referencia da bliblioteca no formato json.
                        val libraryFile = Tools.GLOBAL_GSON.fromJson(
                            read(context.assets.open("support-files.json")),
                            SupportFile::class.java
                        )
                        val integrity =
                            checkFileIntegrity(context, librariesZipFile.md5(), libraryFile.md5)
                        Timber.d("integrity of the file is " + integrity)
                        // se o arquivo não tiver intregidade eu preciso avisar para o usuário que houve um problema
                        // com um arquivo deletar ele e perdir para ele fazer o download novamente
//                        deleteDirecoty(File(context.getExternalFilesDir(null), ".minecraft/libraries"))
                        mDownloader.value?.unpackLibraries(librariesZipFile)
                        withContext(Dispatchers.Main) {

                            loadingState.value = Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN
                        }
                        downloadOneDotSixteen.value = true
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
                    val pixelmonVersion =
                        if (downloadOneDotSixteen.value == true) {
                            downloadTexture.value = true
                            PixelmonVersion.OneDotSixteen
                        } else {
                            PixelmonVersion.OneDotTwelve
                        }
                    mDownloader.value?.downloadTexture(pixelmonVersion)?.await()
                    // here I will call a function to put the texture in the correct place
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

    private fun renameModsFiles(ctx: Context, modVersion: PixelmonVersion) {
        val oneDotSixteenDir =
            File(context.getExternalFilesDir(null), PixelmonVersion.OneDotSixteen.pathMods)
        val oneDotTwelveDir =
            File(context.getExternalFilesDir(null), PixelmonVersion.OneDotTwelve.pathMods)
        val modsDir = File(context.getExternalFilesDir(null), ".minecraft/mods")

        when (modVersion) {
            PixelmonVersion.OneDotTwelve -> {
                modsDir.renameTo(oneDotSixteenDir)
                oneDotTwelveDir.renameTo(modsDir)
            }

            PixelmonVersion.OneDotSixteen -> {
                modsDir.renameTo(oneDotTwelveDir)
                oneDotSixteenDir.renameTo(modsDir)
            }
        }
    }

    private fun overrideTexture(ctx: Context, textureVersion: PixelmonVersion) {
        val textureOneDotTwelve = File(
            ctx.getExternalFilesDir(null),
            ".minecraft/texturas/textureOneDotTwelve.zip"
        )
        val textureOneDotSixteen = File(
            ctx.getExternalFilesDir(null),
            ".minecraft/texturas/textureOneDotSixteen.zip"
        )
        val texture = File(
            ctx.getExternalFilesDir(null),
            ".minecraft/resourcepacks/texture.zip"
        )

        try {
            when(textureVersion) {
                PixelmonVersion.OneDotTwelve -> {
                    texture.writeBytes(textureOneDotTwelve.readBytes())
                }
                PixelmonVersion.OneDotSixteen -> {
                    texture.writeBytes(textureOneDotSixteen.readBytes())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d(e.message)
        }
    }

    private fun overrideConfigurationsFiles(ctx: Context, modVersion: PixelmonVersion) {
        val problemWithMapKeys = Exception("The keys for the files should have the same name")
        val oneDotTwelve = mapOf(
            "options" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/configurationfile/optionsOneDotTwelve.txt"
            ),
            "optionsof" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/configurationfile/optionsofOneDotTwelve.txt"
            )
        )
        val oneDotSixteen = mapOf(
            "options" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/configurationfile/optionsOneDotSixteen.txt"
            ),
            "optionsof" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/configurationfile/optionsofOneDotSixteen.txt"
            )
        )
        val default = mapOf(
            "options" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/minecraft/options.txt"
            ),
            "optionsof" to File(
                ctx.getExternalFilesDir(null),
                ".minecraft/minecraft/optionsof.txt"
            )
        )
        try {
            when (modVersion) {
                PixelmonVersion.OneDotTwelve -> {
                    default.forEach {
                        it.value.writeBytes(
                            oneDotTwelve[it.key]?.readBytes() ?: throw problemWithMapKeys
                        )
                    }
                }

                PixelmonVersion.OneDotSixteen -> {
                    default.forEach {
                        it.value.writeBytes(
                            oneDotSixteen[it.key]?.readBytes() ?: throw problemWithMapKeys
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d(e.message)
        }
    }

    private fun updatePreferencesPixelmonVersion(pixelmonVersion: PixelmonVersion) {
        val isOneDotTwelve = pixelmonVersion == PixelmonVersion.OneDotTwelve
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean(
            "select_version_is_one_dot_twelve", isOneDotTwelve
        ).commit()
        LauncherPreferences.loadPreferences(context)
    }

    fun setupPixelmonLoading() {
        if (callPixelmonLoading.value == false) {
            Timber.d("the value of getOneDotTwelve is " + getOneDotTwelve)
            if (getOneDotTwelve.value == true) {
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
