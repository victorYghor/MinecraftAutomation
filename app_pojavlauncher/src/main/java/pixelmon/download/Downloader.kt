package pixelmon.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
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
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.utils.ZipUtils
import pixelmon.SupportFile
import pixelmon.Texture
import pixelmon.Tools.Timberly
import pixelmon.Tools.checkFileIntegrity
import pixelmon.mods.Mod
import pixelmon.mods.ModFile
import pixelmon.mods.PixelmonVersion
import timber.log.Timber
import java.io.File
import java.util.zip.ZipFile
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
    private val pixelmonTexture = Tools.GLOBAL_GSON.fromJson(
        read(context.assets.open("texture.json")), Texture::class.java
    )
    private val libraries = Tools.GLOBAL_GSON.fromJson(
        read(context.assets.open("support-files.json")),
        SupportFile::class.java
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
        title: String,
        quantity: Int = 0,
        subPath: String
    ) = CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
        val request = DownloadManager
            .Request(uri)
            .setMimeType("application/gzip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title).setDestinationInExternalFilesDir(context, null, subPath)
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
                                    ProgressLayout.setProgress(
                                        ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                                        currentProgress.toCeilInt(),
                                        title
                                    )
                                } else if (currentProgress == 0.0) {
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
                                ProgressLayout.clearProgress(ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE)
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
                            //todo avisar que o download falhou e pedir para o usuário começar de novo o processo de carregamento
                        }
                    }
                }
            }
        }
        id
    }

    private suspend fun downloadMod(
        mod: Mod,
        quantity: Int = 0,
        modVersion: PixelmonVersion
    ): Deferred<Long> {
        Timber.tag(Timberly.downloadProblem).d("Try to download mod %s", mod.name)
        val title = "Baixando o mod ${mod.name}"
        val fileName = mod.artifact.fileName
        return download(
            uri = mod.artifact.url.toUri(),
            title = title,
            quantity = quantity,
            subPath = (if (modVersion == PixelmonVersion.OneDotSixteen) modVersion.pathMods else ".minecraft/mods") + "/" + fileName
        )
    }

    suspend fun downloadOneDotSixteen(): Deferred<Long> {
        val title = "Instalando arquivos necessários para a 1.16"
        return download(
            uri = libraries.link.toUri(),
            title = title,
            subPath = ".minecraft/libraries.zip",
            quantity = 0
        )
    }

    suspend fun downloadTexture(pixelmonVersion: PixelmonVersion): Deferred<Long> {
        val texture = pixelmonTexture
        Timber.d("starting downloading texture " + texture.name)
        val title = "Baixando ${texture.name}"
        File(context.getExternalFilesDir(null), ".minecraft/resourcepacks").mkdirs()
        return download(
            uri = texture.url.toUri(),
            title = title,
            subPath =
            if (pixelmonVersion == PixelmonVersion.OneDotTwelve)
                ".minecraft/mods/${texture.fileName}"
            else
                ".minecraft/modsOneDotSixteen/${texture.fileName}"
        )
    }

    suspend fun downloadModsOneDotTwelve(exclude: List<String> = listOf()): Job {
        val mods = if (exclude.isNotEmpty()) {
            modsOneDotSixteen.filter { !exclude.contains(it.name) }
        } else {
            modsOneDotTwelve.toList()
        }
        return CoroutineScope(Dispatchers.Default).launch {
            for (mod in mods) {
                if (mod.name == "Pixelmon") {
                    downloadMod(mod, modVersion = PixelmonVersion.OneDotTwelve).await()
                } else {
                    downloadMod(
                        mod,
                        mods.size - 1,
                        modVersion = PixelmonVersion.OneDotTwelve
                    ).await()
                }
            }
        }
//        Log.i(TAG, "The value of checkFilesIntegrity is ${checkModsIntegrity(ModVersion.OneDotTwelve)}")
    }

    suspend fun downloadModsOneDotSixteen(): Job {
        File(context.getExternalFilesDir(null), PixelmonVersion.OneDotSixteen.pathMods).mkdirs()
        return CoroutineScope(Dispatchers.Default).launch {
            for (mod in modsOneDotSixteen) {
                if (mod.name == "Pixelmon") {
                    downloadMod(mod, modVersion = PixelmonVersion.OneDotSixteen).await()
                } else {
                    downloadMod(
                        mod,
                        modsOneDotSixteen.size - 1,
                        modVersion = PixelmonVersion.OneDotSixteen
                    ).await()
                }
            }
        }
//        Log.i(TAG, "checkFilesIntegrity = ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
    }

    private fun checkModsIntegrity(modVersion: PixelmonVersion): Boolean {
        // this need to be fixed will use different path for mods
        val mods = File(context.getExternalFilesDir(null), "./minecraft/mods")
        for (file in mods.list()) {
            val path = "./minecraft/mods/$file"
            val mod = if (modVersion == PixelmonVersion.OneDotSixteen) {
                modsOneDotSixteen.find { it.artifact.fileName == file }
            } else {
                modsOneDotSixteen.find { it.artifact.fileName == file }
            }
            if (!checkFileIntegrity(context, path, mod?.artifact?.MD5)) return false
        }
        return true
    }

    fun unpackLibraries(librariesZipFile: File) {
        Timber.tag(TAG).i("unpack Libraries")
        ZipUtils.zipExtract(
            ZipFile(librariesZipFile),
            "",
            File(context.getExternalFilesDir(null), ".minecraft")
        )
        librariesZipFile.delete()
        Timber.tag(TAG).i("finish to unpack libraries of forge")
    }

    /**
     * this will copy texture in the mods folder and put in the modsOneDotSixteen folder
     * this function only can be called when you already download the pixelmon texture
     */
    fun putTextureInOneDotSixteen() =
        CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
            val textureFile =
                File(
                    context.getExternalFilesDir(null),
                    ".minecraft/mods/${pixelmonTexture.fileName}"
                )
            // criar a pasta modsOneDotSixteen
            val modsOneDotSixteenDir =
                File(context.getExternalFilesDir(null), PixelmonVersion.OneDotSixteen.pathMods)
            modsOneDotSixteenDir.mkdirs()

            // cria o arquivo onde ira o a textura
            val outFile = File(
                context.getExternalFilesDir(null),
                "${PixelmonVersion.OneDotSixteen.pathMods}/${pixelmonTexture.fileName}"
            )
            val inputFile = textureFile.inputStream()

            ProgressLayout.setProgress(
                ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                0,
                "copiando a textura"
            )
            inputFile.use { input ->
                Timber.d("the size of the texture is input = ${input.readBytes().size}")
                val quanityOfBytes = inputFile.readBytes().size
                var count = 0.0
                for (byte in input.readBytes()) {
                    val progress = (++count / quanityOfBytes.toDouble() * 100).toCeilInt()
                    ProgressLayout.setProgress(
                        ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE,
                        progress,
                        "copiando a textura"
                    )
                    outFile.appendBytes(byteArrayOf(byte))
                }
            }
        }

}