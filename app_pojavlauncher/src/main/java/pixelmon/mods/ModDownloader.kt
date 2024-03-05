package pixelmon.mods

import android.content.Context
import android.app.DownloadManager
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import java.io.File

class ModDownloader(private val context: Context) {
    companion object {
        private val TAG = "ModDownloader"
    }

    private val mods =
        Tools.GLOBAL_GSON.fromJson(read(context.assets.open("mods.json")), ModFile::class.java).mods
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    fun download(mod: Mod): Long {
        Log.d(TAG, "Try to download mod ${mod.name}")
        val title = "Baixando o mod ${mod.name}"
        File(context.getExternalFilesDir(null), ".minecraft/mods").mkdirs()
        val request = DownloadManager.Request(mod.artifact.url.toUri())
            .setMimeType("application/gzip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDestinationInExternalFilesDir(context, null, ".minecraft/mods/${mod.artifact.fileName}")
        return downloadManager.enqueue(request)
    }

    fun downloadMods() {
        Log.d(TAG, "the mods downloads start")
        mods.forEach { download(it) }
    }
}