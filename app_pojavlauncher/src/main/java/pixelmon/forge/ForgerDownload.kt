package pixelmon.forge

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.utils.ZipUtils
import pixelmon.SupportFile
import pixelmon.Tools.DownloadsIds
import java.io.File
import java.util.zip.ZipFile

class ForgerDownload(private val context: Context) {
    private val TAG = "ForgerInstaller.kt"
    private val libraries = Tools.GLOBAL_GSON.fromJson(read(context.assets.open("support-files.json")), SupportFile::class.java)
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private fun download(): Long {
        val title = "Instalando blibliotecas do forge"
        val request = DownloadManager.Request(libraries.link.toUri())
            .setMimeType("application/zip")
            .setTitle(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, null, ".minecraft/libraries.zip")
        return downloadManager.enqueue(request)
    }
    fun downloadLibraries() {
        DownloadsIds.forge.add(download())
    }
    fun unpackLibraries() {
        Log.i(TAG, "unpakcLibraries")
        ZipUtils.zipExtract(
            ZipFile(File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")),
            "",
            File(context.getExternalFilesDir(null), ".minecraft/libraries")
            )
        Log.i(TAG, "finish to unpack libraries of forge")
    }
}