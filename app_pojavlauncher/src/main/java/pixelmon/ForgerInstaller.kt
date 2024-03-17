package pixelmon

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask
import net.kdt.pojavlaunch.modloaders.ForgeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.utils.FileUtils
import net.kdt.pojavlaunch.utils.ZipUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class ForgerInstaller(private val context: Context): BroadcastReceiver() {
    private val libraries = Tools.GLOBAL_GSON.fromJson(read(context.assets.open("support-files.json")), SupportFile::class.java)
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    fun downloadLibraries(): Long {
        val librariesDir = File(context.getExternalFilesDir(null), ".minecraft/libraries")
        librariesDir.mkdirs()
        val title = "Instalando blibliotecas do forge"
        val request = DownloadManager.Request(libraries.link.toUri())
            .setTitle(title)
            .setMimeType("application/zip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, null, ".minecraft/libraries")
        return downloadManager.enqueue(request)
    }

    fun unpackLibraries() {
        ZipUtils.zipExtract(
            ZipFile(File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")),
            "",
            File(context.getExternalFilesDir(null), ".minecraft/libraries")
            )
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_forge_libraries", true).commit()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val reference = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
        val librariesReference = downloadLibraries()
        if(reference == librariesReference) {
            unpackLibraries()
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_forge_libraries", true).commit()
        }
    }
}