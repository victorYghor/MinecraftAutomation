package pixelmon.mods

import android.content.Context
import android.app.DownloadManager
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import pixelmon.Texture
import java.io.File

class ModDownloader(private val context: Context) {
    companion object {
        private val TAG = "ModDownloader"
    }

    private val mods =
        Tools.GLOBAL_GSON.fromJson(read(context.assets.open("mods.json")), ModFile::class.java).mods
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private val pixelmonTexture = Texture(
        url = "https://download.pixelmonbrasil.com.br/nebula/servers/PixelmonBrasil-1.12.2/files/resourcepacks/Texturas.zip",
        fileName = "Texturas.zip",
        name = "Textura do pixelmon Brasil"
    )
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

    fun downloadTexture(texture: Texture): Long {
        Log.d(TAG, "Straing downloading texture ${texture.name}")
        val title = "Baixando ${texture.name}"
        File(context.getExternalFilesDir(null), ".minecraft/resourcepacks")
        val request = DownloadManager.Request(texture.url.toUri())
            .setTitle(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/gzip")
            .setDestinationInExternalFilesDir(context, null, ".minecraft/resourcepacks/${texture.fileName}")
        return downloadManager.enqueue(request)
    }

    fun downloadMods() {
        Log.d(TAG, "the mods downloads start")
        mods.forEach { download(it) }
        downloadTexture(texture = pixelmonTexture)
    }
}