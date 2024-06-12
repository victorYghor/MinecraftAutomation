package pixelmon.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.kdt.mcgui.ProgressLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Texture
import pixelmon.Tools.Timberly
import pixelmon.Tools.checkFileIntegrity
import pixelmon.mods.Mod
import pixelmon.mods.ModFile
import pixelmon.mods.ModVersion
import timber.log.Timber
import java.io.File
import kotlin.math.ceil

class Downloader(private val context: Context, val viewModel: LauncherViewModel) {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private var currentProgress = 0.0

    /**
     * Convert to the largest Int
     */
    fun Double.toCeilInt() = ceil(this).toInt()

    companion object {
        private val TAG = "Downloader"
    }

    val modsOneDotTwelve = Tools.GLOBAL_GSON.fromJson(
        read(context.assets.open("mods-1.12.json")), ModFile::class.java
    ).mods
    val modsOneDotSixteen = Tools.GLOBAL_GSON.fromJson(
        read(context.assets.open("mods-1.16.json")), ModFile::class.java
    ).mods
    private val pixelmonTexture = Texture(
        url = "https://download.pixelmonbrasil.com.br/nebula/servers/PixelmonBrasil-1.12.2/files/resourcepacks/Texturas.zip",
        fileName = "Texturas.zip",
        name = "Textura do pixelmon Brasil"
    )

    /** Indicate that we would like to update download progress */
    private val UPDATE_DOWNLOAD_PROGRESS = 1


    /**
     * Central function to download files in the app
     * @param quantity for pixelmon need to be 0, of mods if you have more that one mod to download the progress bar is work differently
     *
     */
    @SuppressLint("Range")
    suspend fun download(
        uri: Uri,
        url: String,
        title: String,
        quantity: Int = 0,
    ) = CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
        val request = DownloadManager.Request(uri).setMimeType("application/gzip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title).setDestinationInExternalFilesDir(context, null, ".minecraft/mods/$url")
        // here the download is started
        val id = downloadManager.enqueue(request)

        var isDownloadFinished = false
        while (!isDownloadFinished) {
            delay(100)
            downloadManager!!.query(
                DownloadManager.Query().setFilterById(id)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_RUNNING -> {
                            val totalBytes =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (totalBytes > 0) {
                                val downloadedBytes =
                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                                // It is just a bigger download
                                if (quantity == 0) {
                                    currentProgress = (downloadedBytes * 100.0 / totalBytes)
                                    Timber.d("the progress download is " + currentProgress)
                                    ProgressLayout.setProgress(
                                        ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                                        currentProgress.toCeilInt(),
                                        title
                                    )
                                } else if(currentProgress == 0.0){
                                    ProgressLayout.setProgress(
                                        ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                                        0,
                                        title
                                    )
                                }
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
//                                    Log.d("Downloader", "Download finished with success $title")
                            // um download grande como pixelmon
                            if (quantity == 0) {
                                currentProgress = 100.0
                                isDownloadFinished = true
                                ProgressLayout.setProgress(
                                    ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                                    currentProgress.toCeilInt(),
                                    title
                                )
                                currentProgress = 0.0
                            } else {
                                if (currentProgress == 0.0) {
                                    currentProgress = 100.0 / quantity
                                } else {
                                    // increase in one the number of mods downloaded
                                    currentProgress =
                                        ((currentProgress * quantity + 100) / 100) * 100 / quantity
                                }
                                isDownloadFinished = true
                                ProgressLayout.setProgress(
                                    ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                                    currentProgress.toCeilInt(),
                                    title
                                )
                                if (currentProgress == 100.0) {
                                    currentProgress = 0.0
                                }
                            }
                        }

                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_FAILED -> {
                            isDownloadFinished = true
                            Timber
                            //todo avisar que o download falhou e pedir para o usuário começar de novo o processo de carregamento
                        }
                    }
                }
            }
        }
        id
    }

    private suspend fun downloadMod(mod: Mod, quantity: Int = 0): Deferred<Long> {
        Timber.tag(Timberly.downloadProblem).d("Try to download mod %s", mod.name)
        val title = "Baixando o mod ${mod.name}"
        File(context.getExternalFilesDir(null), ".minecraft/mods").mkdirs()
        return download(
            uri = mod.artifact.url.toUri(),
            url = mod.artifact.fileName,
            title = title,
            quantity = quantity
        )
    }

    suspend fun downloadTexture(): Deferred<Long> {
        val texture = pixelmonTexture
        Timber.d("Straing downloading texture " + texture.name)
        val title = "Baixando ${texture.name}"
        File(context.getExternalFilesDir(null), ".minecraft/resourcepacks").mkdirs()
        return download(uri = texture.url.toUri(), url = texture.fileName, title = title)
    }

    suspend fun downloadModsOneDotTwelve(exclude: List<String> = listOf()): Job {
        Timber.tag(Timberly.downloadProblem).d("the mods downloads start")
            val mods = if (exclude.isNotEmpty()) {
                modsOneDotSixteen.filter { !exclude.contains(it.name) }
            } else {
                modsOneDotTwelve.toList()
            }
            return CoroutineScope(Dispatchers.Default).launch {
                for (mod in mods) {
                    if (mod.name == "Pixelmon") {
                        Timber.tag(Timberly.downloadProblem).d("Chamando a função de instalar mods para baixar o pixelmon")
                        downloadMod(mod).await()
                    } else {
                        downloadMod(mod, mods.size - 1).await()
                    }
                }
                LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_twelve", true).commit()
            }
//        Log.i(TAG, "The value of checkFilesIntegrity is ${checkModsIntegrity(ModVersion.OneDotTwelve)}")
    }

    private fun checkModsIntegrity(modVersion: ModVersion): Boolean {
        val mods = File(context.getExternalFilesDir(null), "./minecraft/mods")
        for (file in mods.list()) {
            val path = "./minecraft/mods/$file"
            val mod = if (modVersion == ModVersion.OneDotSixteen) {
                modsOneDotSixteen.find { it.artifact.fileName == file }
            } else {
                modsOneDotSixteen.find { it.artifact.fileName == file }
            }
            if (!checkFileIntegrity(context, path, mod?.artifact?.MD5)) return false
        }
        return true
    }

    suspend fun downloadModOneDotSixteen() {
        Log.i(TAG, "the mods 1.16 will strat")
//        Log.i(TAG, "The value of checkFilesInregrity is ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
        if (!LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_SIXTEEN) {
            val essentialMods = listOf("MultiplayerMode", "lazydfu", "pixelmon")
            val mods = modsOneDotSixteen.filter { essentialMods.contains(it.name) }
            mods.forEach { downloadMod(it) }
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_sixteen", true)
                .commit()
        }
//        Log.i(TAG, "checkFilesIntegrity = ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
    }
}