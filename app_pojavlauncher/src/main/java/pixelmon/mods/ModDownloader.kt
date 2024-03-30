package pixelmon.mods

import android.content.Context
import android.app.DownloadManager
import android.util.Log
import androidx.core.net.toUri
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Pixelmon
import pixelmon.State
import pixelmon.Texture
import pixelmon.Tools.checkFileIntegrity
import pixelmon.Tools.md5
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
enum class ModVersion {
    OneDotTwelve,
    OneDotSixteen
}
class ModDownloader(private val context: Context) {
    companion object {
        private val TAG = "ModDownloader"
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
        File(context.getExternalFilesDir(null), ".minecraft/resourcepacks").mkdirs()
        val request = DownloadManager.Request(texture.url.toUri())
            .setTitle(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/gzip")
            .setDestinationInExternalFilesDir(context, null, ".minecraft/resourcepacks/${texture.fileName}")
        return downloadManager.enqueue(request)
    }

    fun downloadModsOneDotTwelve(exclude:List<String> = listOf()) {
        Log.d(TAG, "the mods downloads start")
        if(!LauncherPreferences.DOWNLOAD_ONE_DOT_TWELVE) {
            Pixelmon.state = State.DOWNLOAD_MODS

            var mods = if(exclude.isNotEmpty()) {
                modsOneDotSixteen.filter { !exclude.contains(it.name) }
            } else {
                modsOneDotTwelve.toList()
            }
            mods.forEach{
                download(it)
            }
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_one_dot_twelve", true).commit()
            Pixelmon.state = State.PLAY
        }
        Log.i(TAG, "The value of checkFilesIntegrity is ${checkModsIntegrity(ModVersion.OneDotTwelve)}")
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
        Log.i(TAG, "The value of checkFilesInregrity is ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
        if(!LauncherPreferences.DOWNLOAD_ONE_DOT_SIXTEEN) {
            Pixelmon.state = State.DOWNLOAD_MODS
            val essentialMods = listOf("MultiplayerMode", "lazydfu", "pixelmon")
            val mods = modsOneDotSixteen.filter { essentialMods.contains(it.name) }
            mods.forEach { download(it) }
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("download_one_dot_sixteen", true).commit()
            Pixelmon.state = State.PLAY
        }
        Log.i(TAG, "checkFilesIntegrity = ${checkModsIntegrity(ModVersion.OneDotSixteen)}")
    }
}