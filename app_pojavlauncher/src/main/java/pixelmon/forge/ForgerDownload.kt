package pixelmon.forge

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.ZipUtils
import pixelmon.SupportFile
import pixelmon.Tools.DownloadsIds
import java.io.File
import java.util.zip.ZipFile

class ForgerDownload(private val context: Context): Runnable {
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
    fun unpackLibraries() {
        Log.i(TAG, "unpack Libraries")
        val librariesZipFile = File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")
        ZipUtils.zipExtract(
            ZipFile(librariesZipFile),
            "",
            File(context.getExternalFilesDir(null), ".minecraft")
            )
        librariesZipFile.delete()
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_one_dot_sixteen", true).commit()
        Log.i(TAG, "finish to unpack libraries of forge")
    }

    override fun run() {
        Log.d(TAG, "iniciando o download do minecraft 1.16")
        DownloadsIds.forge.add(download())
    }
}