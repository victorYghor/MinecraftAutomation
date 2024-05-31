package pixelmon.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Texture
import pixelmon.Tools.checkFileIntegrity
import pixelmon.mods.Mod
import pixelmon.mods.ModFile
import pixelmon.mods.ModVersion
import java.io.File
import java.util.concurrent.Executors

class Downloader(private val context: Context) {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    companion object {
        private val TAG = "Downloader"
    }
    val modsOneDotTwelve =
        Tools.GLOBAL_GSON.fromJson(
            read(context.assets.open("mods-1.12.json")),
            ModFile::class.java
        ).mods
    val modsOneDotSixteen = Tools.GLOBAL_GSON.fromJson(
        read(context.assets.open("mods-1.16.json")),
        ModFile::class.java
    ).mods
    private val pixelmonTexture = Texture(
        url = "https://download.pixelmonbrasil.com.br/nebula/servers/PixelmonBrasil-1.12.2/files/resourcepacks/Texturas.zip",
        fileName = "Texturas.zip",
        name = "Textura do pixelmon Brasil"
    )

    /** Indicate that we would like to update download progress */
    private val UPDATE_DOWNLOAD_PROGRESS = 1
    /** Use a background thread to check the progress of downloading */
    private val executor = Executors.newFixedThreadPool(1)
    /** Use a handler to update progress bar on the main thread */
    private val mainHandler: Handler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == UPDATE_DOWNLOAD_PROGRESS) {
            val downloadProgress: Int = msg.arg1

            // Update your progress bar here.
            Log.d(TAG, "Download progress: $downloadProgress")
        }
        true
    }

    /**
     * Central function to download files in the app
     */
    @SuppressLint("Range")
    fun download(uri: Uri, url: String, title: String): Long {
        val request = DownloadManager.Request(uri)
            .setMimeType("application/gzip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDestinationInExternalFilesDir(context, null, ".minecraft/mods/$url")
        val id = downloadManager.enqueue(request)

//        executor.execute {
//            var progress = 0
//            var isDownloadFinished = false
//            while (!isDownloadFinished) {
//                val cursor = downloadManager!!.query(
//                    DownloadManager.Query().setFilterById(id)
//                )
//                if (cursor.moveToFirst()) {
//                    val downloadStatus =
//                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                    when (downloadStatus) {
//                        DownloadManager.STATUS_RUNNING -> {
//                            val totalBytes =
//                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                            if (totalBytes > 0) {
//                                val downloadedBytes =
//                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                                progress = (downloadedBytes * 100 / totalBytes).toInt()
//                                Log.d(TAG, "the progress download is $progress")
//                            }
//                        }
//                        DownloadManager.STATUS_SUCCESSFUL -> {
//                            progress = 100
//                            isDownloadFinished = true
//                        }
//
//                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
//                        DownloadManager.STATUS_FAILED -> isDownloadFinished = true
//                    }
//                    val message: Message = Message.obtain()
//                    message.what = UPDATE_DOWNLOAD_PROGRESS
//                    message.arg1 = progress
//                    mainHandler.sendMessage(message)
//                }
//            }
//        }
        return id
    }

    fun downloadMod(mod: Mod): Long {
        Log.d(TAG, "Try to download mod ${mod.name}")
        val title = "Baixando o mod ${mod.name}"
        File(context.getExternalFilesDir(null), ".minecraft/mods").mkdirs()
        return download(uri = mod.artifact.url.toUri(), url = mod.artifact.fileName, title = title)
    }

    fun downloadTexture(texture: Texture): Long {
        Log.d(TAG, "Straing downloading texture ${texture.name}")
        val title = "Baixando ${texture.name}"
        File(context.getExternalFilesDir(null), ".minecraft/resourcepacks").mkdirs()
        return download(uri = texture.url.toUri(), url = texture.fileName, title = title)
    }

    fun downloadModsOneDotTwelve(exclude:List<String> = listOf()) {
        Log.d(TAG, "the mods downloads start")
        if(!LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_TWELVE) {

            val mods = if(exclude.isNotEmpty()) {
                modsOneDotSixteen.filter { !exclude.contains(it.name) }
            } else {
                modsOneDotTwelve.toList()
            }
            downloadMod(mods.first())
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_twelve", true).commit()
        }
//        Log.i(TAG, "The value of checkFilesIntegrity is ${checkModsIntegrity(ModVersion.OneDotTwelve)}")
    }

    private fun checkModsIntegrity(modVersion: ModVersion): Boolean {
        val mods = File(context.getExternalFilesDir(null), "./minecraft/mods")
        for(file in mods.list()){
            val path = "./minecraft/mods/$file"
            val mod =
                if(modVersion == ModVersion.OneDotSixteen) {
                    modsOneDotSixteen.find { it.artifact.fileName == file}
                } else {
                    modsOneDotSixteen.find{ it.artifact.fileName == file}
                }
            if(!checkFileIntegrity(context, path, mod?.artifact?.MD5)) return false
        }
        return true
    }

    fun downloadModOneDotSixteen() {
        Log.i(TAG, "the mods 1.16 will strat")
//        Log.i(TAG, "The value of checkFilesInregrity is ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
        if(!LauncherPreferences.DOWNLOAD_MOD_ONE_DOT_SIXTEEN) {
            val essentialMods = listOf("MultiplayerMode", "lazydfu", "pixelmon")
            val mods = modsOneDotSixteen.filter { essentialMods.contains(it.name) }
            mods.forEach { downloadMod(it) }
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_mod_one_dot_sixteen", true).commit()
        }
//        Log.i(TAG, "checkFilesIntegrity = ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
    }
}